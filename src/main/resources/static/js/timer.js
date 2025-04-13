function startCountdown({ seconds, onCompleteRedirect, updateVisuals = false }) {
    const countdownEl = document.getElementById('countdown');
    const handEl = document.getElementById('hand');
    const overlayEl = document.getElementById('overlay');
    let current = seconds;

    function update() {
        countdownEl.textContent = current;

        if (updateVisuals) {
            if (handEl) {
                const rotation = (360 / seconds) * (seconds - current);
                handEl.style.transform = `rotate(${rotation - 90}deg)`;
            }
            if (overlayEl) {
                const opacity = (seconds - current) / seconds;
                overlayEl.style.opacity = opacity * 0.7;
            }
        }

        current--;
        if (current < 0) {
            window.location.href = onCompleteRedirect;
        } else {
            setTimeout(update, 1000);
        }
    }

    update();
}
