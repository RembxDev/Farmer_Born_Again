class AudioManager {
    constructor() {
        this.sounds = {};
        this.muted = false;
    }

    preload(sounds) {
        for (const [key, path] of Object.entries(sounds)) {
            const audio = new Audio(path);
            audio.volume = 0.5;
            audio.load();
            this.sounds[key] = audio;
        }
    }

    play(name) {
        if (!this.muted && this.sounds[name]) {
            this.sounds[name].currentTime = 0;
            this.sounds[name].play();
        }
    }

    toggleMute() {
        this.muted = !this.muted;
    }
}

const audioManager = new AudioManager();

document.addEventListener("DOMContentLoaded", () => {
    audioManager.preload({
        buy: "/sounds/buy.mp3",
        sell: "/sounds/sell.mp3",
        morning: "/sounds/morning.mp3",
        night: "/sounds/night.mp3",
        wheat: "/sounds/wheat.mp3",
        rabbit: "/sounds/rabbit.mp3",
        chicken: "/sounds/chicken.mp3",
        pig: "/sounds/pig.mp3",
        sheep: "/sounds/sheep.mp3",
        cow: "/sounds/cow.mp3",
        horse: "/sounds/horse.mp3",
        dog: "/sounds/dog.mp3",
        fox: "/sounds/fox.mp3",
        wolf: "/sounds/wolf.mp3",
        dice1: "/sounds/dice1.mp3",
        dice2: "/sounds/dice2.mp3",
        dice3: "/sounds/dice3.mp3",
        eat: "/sounds/eat.mp3"
    });
});

function playWithAnimal(mainSound, animalSound, delay = 200) {
    audioManager.play(mainSound);
    setTimeout(() => {
        audioManager.play(animalSound);
    }, delay);
}
