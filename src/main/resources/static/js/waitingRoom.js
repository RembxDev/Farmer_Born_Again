let stompClient = null;
let playerName = document.getElementById("playerName").textContent;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Połączono z STOMP: ' + frame);

        stompClient.subscribe('/topic/lobby/chat', function(message) {
            const chatMessage = JSON.parse(message.body);
            console.log("Odebrano wiadomość czatu:", chatMessage);
            addChatMessage(chatMessage.sender, chatMessage.content, chatMessage.timestamp);
        });

        stompClient.subscribe('/topic/lobby/status', function(message) {
            const statusMessage = JSON.parse(message.body);
            console.log("Odebrano status gracza:", statusMessage);
            updatePlayerStatus(statusMessage);
        });

        stompClient.subscribe('/topic/lobby/gameStartStatus', function(message) {
            const status = JSON.parse(message.body);
            if (status.type === "GAME_STARTED") {
                console.log("Gra się zaczyna! Przenoszę do farmy...");
                window.location.href = "/farm/";
            }
        });

        stompClient.subscribe('/topic/lobby/playerList', function(message) {
            const players = JSON.parse(message.body);
            const playersList = document.getElementById('playersList');
            playersList.innerHTML = '';
            players.forEach(player => {
                const li = document.createElement('li');
                li.id = "player-" + player.playerName;
                li.textContent = player.status === "READY"
                    ? `${player.playerName} ✅`
                    : `${player.playerName} ⏳`;
                playersList.appendChild(li);
            });
        });

    });
}

function addChatMessage(sender, content, timestamp) {
    const chatWindow = document.getElementById('chatWindow');
    const messageElement = document.createElement('div');
    messageElement.textContent = `[${sender}] ${content}`;
    chatWindow.appendChild(messageElement);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

function sendChatMessage() {
    const chatInput = document.getElementById('chatInput');
    const content = chatInput.value.trim();
    if (content && stompClient) {
        const message = {
            type: 'CHAT',
            sender: playerName,
            content: content
        };
        console.log("Wysyłanie wiadomości czatu:", message);
        stompClient.send("/app/lobby/chat", {}, JSON.stringify(message));
        chatInput.value = '';
    }
}

function updatePlayerStatus(statusMsg) {
    const playersList = document.getElementById('playersList');
    const existingPlayer = document.getElementById("player-" + statusMsg.playerName);

    if (statusMsg.status === "JOINED") {
        if (!existingPlayer) {
            const li = document.createElement('li');
            li.id = "player-" + statusMsg.playerName;
            li.textContent = `${statusMsg.playerName} ⏳`;
            playersList.appendChild(li);
        }
        if (existingPlayer) {
            existingPlayer.textContent = `${statusMsg.playerName} ⏳`;
        }
    } else if (statusMsg.status === "READY") {
        if (existingPlayer) {
            existingPlayer.textContent = `${statusMsg.playerName} ✅`;
        }
    } else if (statusMsg.status === "LEFT") {
        if (existingPlayer) {
            playersList.removeChild(existingPlayer);
        }
    }
}

function markReady() {
    fetch("/ready", {
        method: "POST"
    }).then(res => {
        if (!res.ok) {
            alert("Błąd: nie udało się oznaczyć jako gotowy");
        } else {
            console.log("Zgłoszono gotowość.");
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {
    console.log("Ładowanie strony poczekalni, nawiązywanie połączenia...");
    connect();
});
