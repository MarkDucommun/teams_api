var uploader = document.querySelector("#uploader");

var preview = document.querySelector("#preview");

var label = document.querySelector("#file-label");

var loader = document.querySelector(".loader");

var players = document.querySelector("#players");

var submit = document.querySelector("#submit");

var thanks = document.querySelector("#thanks");

var imageID = null;

function uploadPhoto(file, callback) {

    var url = '/photos';

    var xhr = new XMLHttpRequest();

    var fd = new FormData();

    xhr.open("POST", url, true);

    xhr.onreadystatechange = function () {

        if (xhr.readyState == 4 && xhr.status == 200) {

            callback(xhr)
        }
    };

    fd.append("file", file);

    xhr.send(fd);
}

function setPlayers() {

    var url = "/players"

    var xhr = new XMLHttpRequest();

    xhr.open("GET", url, true);

    xhr.onreadystatechange = function () {

        if (xhr.readyState == 4 && xhr.status == 200) {

            var playerList = JSON.parse(xhr.response);

            playerList.forEach(function (value) {
                var option = document.createElement("option");

                option.value = value.id;
                option.innerText = value.name;

                players.appendChild(option);
            })
        }
    };

    xhr.send();
}

function createRace(selectPlayers) {

    var url = "/races?imageId=" + imageID + "&players=" + selectPlayers.join();

    var xhr = new XMLHttpRequest();

    xhr.open("POST", url, true);

    xhr.onreadystatechange = function () {

        if (xhr.readyState == 4 && xhr.status == 200) {

            submit.classList.add("hide");
            preview.classList.add("hide");
            players.classList.add("hide");
            thanks.classList.remove("hide");
        }
    };

    xhr.send();
}

label.addEventListener("click", function () {

    label.classList.add('hide');
});

uploader.addEventListener("change", function () {

    loader.classList.remove('hide');

    uploadPhoto(this.files[0], function(xhr) {

        loader.classList.add("hide");

        var response = JSON.parse(xhr.response)

        preview.src = response.image.path;

        imageID = response.image.id;

        preview.classList.remove("hide");

        players.classList.remove("hide");

        submit.classList.remove("hide");
    });
});

submit.addEventListener("click", function () {

    var selectedPlayers = [];

    for (var i = 0; i < players.selectedOptions.length; i++) {
        selectedPlayers.push(players.selectedOptions[i].value)
    }

    createRace(selectedPlayers)
});

setPlayers();