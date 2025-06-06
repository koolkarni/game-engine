  async function createGame() {
    const playerId = document.getElementById("createPlayerId").value;
    const res = await fetch(`/api/games/create?playerId=${playerId}`, {
      method: "POST"
    });

    const game = await res.json();
    document.getElementById("createResult").innerText = `Game created with ID: ${game.id}`;

    // Fill other fields
    document.getElementById("viewGameId").value = game.id;
    document.getElementById("joinGameId").value = game.id;
    document.getElementById("moveGameId").value = game.id;

    // ✅ Correct: pass game.id explicitly
    viewGamePlayers(game.id);
  }

 async function joinGame() {
  const gameId = document.getElementById("joinGameId").value;
  const playerId = document.getElementById("joinPlayerId").value;
  const res = await fetch(`/api/games/${gameId}/join?playerId=${playerId}`, {
    method: "POST"
  });
  const game = await res.json();
  document.getElementById("joinResult").innerText = `Player ${playerId} joined Game ID: ${game.id}`;

  document.getElementById("viewGameId").value = game.id;
  document.getElementById("moveGameId").value = game.id;

  viewGamePlayers(game.id); // ✅ fixed
}

    async function makeMove() {
    const gameId = document.getElementById("moveGameId").value;
    const playerId = document.getElementById("movePlayerId").value;
    const row = document.getElementById("moveRow").value;
    const col = document.getElementById("moveCol").value;
    const resultEl = document.getElementById("moveResult");

    const res = await fetch(`/api/games/${gameId}/move`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ playerId, row, col })
    });

    if (!res.ok) {
      try {
        const errorBody = await res.json();
        resultEl.innerText = errorBody.message || "Move failed.";
      } catch {
        const fallback = await res.text();
        resultEl.innerText = fallback || "Move failed.";
      }
      resultEl.className = "text-red-600 text-sm mt-2 font-medium";
      return;
    }

    const { game, board } = await res.json();
    renderBoard(board);
    resultEl.innerText = `Move OK. Status: ${game.status}, Winner: ${game.winnerId || "TBD"}`;
    resultEl.className = "text-green-700 text-sm mt-2 font-medium";
    viewGamePlayers();
  }

   async function viewGamePlayers(gameId = null) {
    if (!gameId) {
      gameId = document.getElementById("viewGameId").value;
    }

    const res = await fetch(`/api/games/${gameId}`);
    const box = document.getElementById("playerListBox");

    if (!res.ok) {
      box.innerText = `Game ${gameId} not found.`;
      return;
    }

    const { game, board } = await res.json();
    const p1 = game.player1Id ? `Player 1: ${game.player1Id}` : "Player 1: Not joined";
    const p2 = game.player2Id ? `Player 2: ${game.player2Id}` : "Player 2: Not joined";

    box.innerText = `Game: ${game.id}\n${p1}\n${p2}`;
    renderBoard(board); // if available
  }

  function renderBoard(grid) {
    const boardEl = document.getElementById("liveBoard");
    boardEl.innerHTML = ''; // Clear previous

    for (let row = 0; row < 3; row++) {
      for (let col = 0; col < 3; col++) {
        const val = grid[row][col] || '-';
        const cell = document.createElement("div");
        cell.textContent = val;
        cell.className = "border bg-white flex items-center justify-center";
        cell.style.width = "50px";
        cell.style.height = "50px";
        cell.style.fontSize = "8px"; // Fit text
        boardEl.appendChild(cell);
      }
    }
  }

  async function loadLeaderboard() {
    const res = await fetch("/api/games/leaderboard");
    if (!res.ok) return;

    const data = await res.json();

    const efficiencyBody = document.getElementById("leaderboardBody");
    const winsBody = document.getElementById("leaderboardWinsBody");

    efficiencyBody.innerHTML = "";
    winsBody.innerHTML = "";

    (data.topByEfficiency || []).forEach(player => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td class="border px-3 py-1">${player.playerId}</td>
        <td class="border px-3 py-1">${player.wins}</td>
        <td class="border px-3 py-1">${player.efficiency.toFixed(2)}</td>
      `;
      efficiencyBody.appendChild(row);
    });

    (data.topByWins || []).forEach(player => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td class="border px-3 py-1">${player.playerId}</td>
        <td class="border px-3 py-1">${player.wins}</td>
        <td class="border px-3 py-1">${player.efficiency.toFixed(2)}</td>
      `;
      winsBody.appendChild(row);
    });
  }

async function simulateCustomGames() {
  const totalUsers = parseInt(document.getElementById("totalUsers").value);
  const totalGames = parseInt(document.getElementById("totalGames").value);

  const statusEl = document.getElementById("simulationStatus");
  const simulationLog = document.getElementById("simulationLog");

  statusEl.innerText = `Simulating ${totalGames} games with ${totalUsers} users...`;
  simulationLog.innerText = "";

  const userIds = Array.from({ length: totalUsers }, (_, i) => i + 1);

  for (let i = 0; i < totalGames; i++) {
    const [p1, p2] = getRandomPair(userIds);
    const gameRes = await fetch(`/api/games/create?playerId=${p1}`, { method: "POST" });
    const game = await gameRes.json();

    await fetch(`/api/games/${game.id}/join?playerId=${p2}`, { method: "POST" });

    let currentPlayer = p1;
    const grid = Array.from({ length: 3 }, () => Array(3).fill(0));
    simulationLog.innerText += `🎮 Game #${game.id} [P1: ${p1}, P2: ${p2}]\n`;

    for (let move = 0; move < 9; move++) {
      const [row, col] = getRandomEmptyCell(grid);
      grid[row][col] = currentPlayer;

      try {
        const res = await fetch(`/api/games/${game.id}/move`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ row, col, playerId: currentPlayer })
        });

        const { game: updatedGame, board } = await res.json();

        simulationLog.innerText += `  ✅ Move #${move + 1}: Player ${currentPlayer} → (${row},${col})\n`;

        if (updatedGame.status === "COMPLETE") {
          simulationLog.innerText += `  🏁 Game Over. Winner: ${updatedGame.winnerId || "Draw"}\n\n`;
          break;
        }

        currentPlayer = currentPlayer === p1 ? p2 : p1;
      } catch {
        simulationLog.innerText += `  ❌ Invalid move by ${currentPlayer}, retrying...\n`;
      }
    }
  }

  statusEl.innerText = `✅ Simulated ${totalGames} games. Leaderboard updated.`;
  loadLeaderboard();
}

function getRandomPair(arr) {
  const shuffled = [...arr].sort(() => 0.5 - Math.random());
  return shuffled.slice(0, 2);
}

function getRandomEmptyCell(grid) {
  const empty = [];
  for (let r = 0; r < 3; r++)
    for (let c = 0; c < 3; c++)
      if (grid[r][c] === 0) empty.push([r, c]);
  return empty[Math.floor(Math.random() * empty.length)];
}