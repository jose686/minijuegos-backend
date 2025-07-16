document.addEventListener('DOMContentLoaded', () => {
    const colorButton = document.getElementById('colorButton');
    const box = document.getElementById('box');
    const colors = ['red', 'blue', 'green', 'purple', 'orange', 'black'];
    let currentColorIndex = 0;

    colorButton.addEventListener('click', () => {
        currentColorIndex = (currentColorIndex + 1) % colors.length;
        box.style.backgroundColor = colors[currentColorIndex];
    });
});