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
    if (statusMsg.status === "JOINED") {
        const li = document.createElement('li');
        li.id = "player-" + statusMsg.playerName;
        li.textContent = statusMsg.playerName;
        playersList.appendChild(li);
        console.log("Gracz dołączył:", statusMsg.playerName);
    } else if (statusMsg.status === "LEFT") {
        const playerElement = document.getElementById("player-" + statusMsg.playerName);
        if (playerElement) {
            playersList.removeChild(playerElement);
            console.log("Gracz opuścił:", statusMsg.playerName);
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    console.log("Ładowanie strony poczekalni, nawiązywanie połączenia...");
    connect();
});
