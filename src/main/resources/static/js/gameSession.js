
let stompClient = null;
let playerName = document.getElementById("playerName").textContent;
let gameId = document.getElementById("gameId").textContent;

function connectGame() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Połączono z sesją gry, frame: ' + frame);

        stompClient.subscribe('/topic/game/' + gameId, function(message) {
            const event = JSON.parse(message.body);
            console.log("Odebrano komunikat z gry:", event);
            addGameLog(event.description);
        });
    });
}

function sendFeedAction() {
    if (stompClient) {
        const actionEvent = {
            action: "FEED_ANIMAL",
            player: playerName,
            targetId: 42,
            description: playerName + " nakarmił zwierzę o ID 42"
        };
        console.log("Wysyłanie akcji:", actionEvent);
        stompClient.send("/app/game/" + gameId + "/action", {}, JSON.stringify(actionEvent));
    } else {
        console.warn("Brak połączenia WebSocket. Akcja nie została wysłana.");
    }
}

function addGameLog(message) {
    const gameLog = document.getElementById('gameLog');
    const logEntry = document.createElement('div');
    logEntry.textContent = message;
    gameLog.appendChild(logEntry);
    gameLog.scrollTop = gameLog.scrollHeight;
}

document.addEventListener('DOMContentLoaded', function() {
    console.log("Ładowanie strony, nawiązywanie połączenia z sesją gry...");
    connectGame();
});
