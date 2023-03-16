'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageArea = document.querySelector('#messageArea');
const connectingElement = document.querySelector('.connecting');

let stompClient = null;
let username = null;

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


function onConnected() {
    stompClient.subscribe('/topic', onMessageReceived);
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )
    connectingElement.classList.add('hidden');
}


function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function onMessageReceived(payload) {
    const payloadBody = JSON.parse(payload.body);
    const messageElement = document.createElement('li');

    const imageElement = document.createElement('img');
    imageElement.src = payloadBody.url;
    imageElement.width = 250;
    image.style.cursor = 'pointer';
    //open in new tab
    imageElement.onclick = function () {
        window.open(payloadBody.url, '_blank');
    }
    messageElement.appendChild(imageElement);

    const textElement = document.createElement('p');
    textElement.innerText = payloadBody.description;
    messageElement.appendChild(textElement);

    const confidenceElement = document.createElement('p');
    confidenceElement.innerText = payloadBody.confidence;
    messageElement.appendChild(confidenceElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

usernameForm.addEventListener('submit', connect, true)
