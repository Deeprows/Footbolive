(function () {

"use strict";

/* =========================================================
ELEMENTS
========================================================= */

const player =
document.getElementById("player");

const video =
document.getElementById("video");

const watchOverlay =
document.getElementById("watchOverlay");

const watchButton =
document.getElementById("watchButton");

const playButton =
document.getElementById("playButton");

const centerPlay =
document.getElementById("centerPlay");

const muteButton =
document.getElementById("muteButton");

const volumeSlider =
document.getElementById("volumeSlider");

const fullscreenButton =
document.getElementById("fullscreenButton");

const qualityButton =
document.getElementById("qualityButton");

const qualityMenu =
document.getElementById("qualityMenu");

const loadingScreen =
document.getElementById("loadingScreen");

const errorScreen =
document.getElementById("errorScreen");

const errorMessage =
document.getElementById("errorMessage");

const retryButton =
document.getElementById("retryButton");

/* =========================================================
BASIC SAFETY CHECK
========================================================= */

if (!player || !video) {

console.error(
  "Deeprowss Player: Required player elements are missing."
);

return;

}

/* =========================================================
STREAM URL
========================================================= */

function getStreamUrl() {

/*
 * Supported format:
 *
 * player.html#https://example.com/live/index.m3u8
 */

const hash =
  window.location.hash.substring(1);

if (!hash) {
  return "";
}

try {

  return decodeURIComponent(hash);

} catch (error) {

  return hash;

}

}

/* =========================================================
HLS
========================================================= */

let hls = null;

let currentQuality = -1;

let retryTimer = null;

let hideControlsTimer = null;

/* =========================================================
LOADING
========================================================= */

function showLoading() {

if (loadingScreen) {
  loadingScreen.hidden = false;
}

}

function hideLoading() {

if (loadingScreen) {
  loadingScreen.hidden = true;
}

}

/* =========================================================
ERROR
========================================================= */

function showError(message) {

hideLoading();

if (errorMessage) {

  errorMessage.textContent =
    message ||
    "Unable to load this stream.";

}

if (errorScreen) {
  errorScreen.hidden = false;
}

}

function hideError() {

if (errorScreen) {
  errorScreen.hidden = true;
}

}

/* =========================================================
PLAY / PAUSE
========================================================= */

function updatePlayButton() {

const paused =
  video.paused;

if (playButton) {

  playButton.textContent =
    paused ? "▶" : "❚❚";

  playButton.setAttribute(
    "aria-label",
    paused ? "Play" : "Pause"
  );

}

if (centerPlay) {

  centerPlay.textContent =
    paused ? "▶" : "❚❚";

}

}

function togglePlay() {

if (video.paused) {

  video.play().catch(
    function (error) {

      console.log(
        "Play prevented:",
        error
      );

    }
  );

} else {

  video.pause();

}

}

/* =========================================================
MUTE
========================================================= */

function updateMuteButton() {

if (!muteButton) {
  return;
}

if (
  video.muted ||
  video.volume === 0
) {

  muteButton.textContent =
    "🔇";

} else {

  muteButton.textContent =
    "🔊";

}

}

function toggleMute() {

video.muted =
  !video.muted;

updateMuteButton();

}

/* =========================================================
VOLUME
========================================================= */

function updateVolume() {

if (!volumeSlider) {
  return;
}

const value =
  Number(volumeSlider.value);

video.volume =
  Math.max(
    0,
    Math.min(1, value)
  );

if (video.volume > 0) {
  video.muted = false;
}

updateMuteButton();

}

/* =========================================================
FULLSCREEN
========================================================= */

async function toggleFullscreen() {

try {

  if (!document.fullscreenElement) {

    if (player.requestFullscreen) {

      await player.requestFullscreen();

    } else if (
      video.webkitEnterFullscreen
    ) {

      video.webkitEnterFullscreen();

    }

  } else {

    if (document.exitFullscreen) {

      await document.exitFullscreen();

    }

  }

} catch (error) {

  console.log(
    "Fullscreen unavailable:",
    error
  );

}

}

/* =========================================================
QUALITY MENU
========================================================= */

function clearQualityMenu() {

if (!qualityMenu) {
  return;
}

qualityMenu
  .querySelectorAll(
    ".quality-option"
  )
  .forEach(
    function (button) {

      button.remove();

    }
  );

}

function addQualityButton(
index,
label
) {

if (!qualityMenu) {
  return;
}

const button =
  document.createElement("button");

button.type =
  "button";

button.className =
  "quality-option";

button.dataset.quality =
  String(index);

button.textContent =
  label;

button.addEventListener(
  "click",
  function (event) {

    event.stopPropagation();

    setQuality(index);

  }
);

qualityMenu.appendChild(
  button
);

}

function buildQualityMenu() {

if (!qualityMenu) {
  return;
}

clearQualityMenu();


/* AUTO */

addQualityButton(
  -1,
  "Auto"
);


/* AVAILABLE QUALITIES */

if (
  !hls ||
  !hls.levels ||
  !hls.levels.length
) {

  setQualityActive(
    -1
  );

  return;

}


hls.levels.forEach(
  function (level, index) {

    const height =
      level.height;

    const label =
      height
        ? height + "p"
        : "Quality " + (index + 1);

    addQualityButton(
      index,
      label
    );

  }
);


setQualityActive(
  currentQuality
);

}

function setQualityActive(
quality
) {

if (!qualityMenu) {
  return;
}

qualityMenu
  .querySelectorAll(
    ".quality-option"
  )
  .forEach(
    function (button) {

      button.classList.toggle(
        "active",
        Number(
          button.dataset.quality
        ) === quality
      );

    }
  );

}

function setQuality(
quality
) {

currentQuality =
  quality;

if (hls) {

  hls.currentLevel =
    quality;

}

setQualityActive(
  quality
);

if (qualityMenu) {
  qualityMenu.hidden = true;
}

}

/* =========================================================
CONTROLS AUTO HIDE
========================================================= */

function showControls() {

player.classList.add(
  "controls-visible"
);

player.classList.remove(
  "controls-hidden"
);


clearTimeout(
  hideControlsTimer
);


hideControlsTimer =
  setTimeout(
    function () {

      if (!video.paused) {

        player.classList.add(
          "controls-hidden"
        );

        player.classList.remove(
          "controls-visible"
        );

      }

    },
    3000
  );

}

function showControlsForever() {

clearTimeout(
  hideControlsTimer
);

player.classList.add(
  "controls-visible"
);

player.classList.remove(
  "controls-hidden"
);

}

/* =========================================================
DESTROY HLS
========================================================= */

function destroyHls() {

if (!hls) {
  return;
}

try {

  hls.destroy();

} catch (error) {

  console.log(
    "HLS destroy error:",
    error
  );

}

hls = null;

}

/* =========================================================
LOAD STREAM
========================================================= */

function loadStream() {

const streamUrl =
  getStreamUrl();


if (!streamUrl) {

  showError(
    "No m3u8 stream was provided."
  );

  return;

}


hideError();

showLoading();

destroyHls();


/*
 * Clear the previous video source.
 */

video.removeAttribute(
  "src"
);

video.load();


/* =======================================================
   NATIVE HLS
   ======================================================= */

if (
  video.canPlayType(
    "application/vnd.apple.mpegurl"
  )
) {

  video.src =
    streamUrl;


  const nativeReady =
    function () {

      hideLoading();

      hideError();

      video.removeEventListener(
        "loadedmetadata",
        nativeReady
      );

    };


  const nativeError =
    function () {

      showError(
        "The stream could not be loaded."
      );

      video.removeEventListener(
        "error",
        nativeError
      );

    };


  video.addEventListener(
    "loadedmetadata",
    nativeReady
  );


  video.addEventListener(
    "error",
    nativeError
  );


  return;

}


/* =======================================================
   HLS.JS
   ======================================================= */

if (
  window.Hls &&
  window.Hls.isSupported()
) {

  hls =
    new window.Hls({

      enableWorker: true,

      lowLatencyMode: true,

      backBufferLength: 30,

      liveSyncDurationCount: 3,

      maxBufferLength: 30

    });


  hls.loadSource(
    streamUrl
  );

  hls.attachMedia(
    video
  );


  /* =====================================================
     MANIFEST
     ===================================================== */

  hls.on(
    window.Hls.Events.MANIFEST_PARSED,
    function () {

      hideLoading();

      hideError();

      currentQuality =
        -1;

      buildQualityMenu();

      /*
       * Start in Auto quality.
       */

      hls.currentLevel =
        -1;

    }
  );


  /* =====================================================
     QUALITY SWITCH
     ===================================================== */

  hls.on(
    window.Hls.Events.LEVEL_SWITCHED,
    function () {

      if (
        currentQuality === -1
      ) {

        setQualityActive(
          -1
        );

      }

    }
  );


  /* =====================================================
     HLS ERROR HANDLING
     ===================================================== */

  hls.on(
    window.Hls.Events.ERROR,
    function (
      event,
      data
    ) {

      console.log(
        "HLS error:",
        data
      );


      if (!data.fatal) {
        return;
      }


      /* NETWORK ERROR */

      if (
        data.type ===
        window.Hls.ErrorTypes.NETWORK_ERROR
      ) {

        console.log(
          "HLS network error. Reconnecting..."
        );


        try {

          hls.startLoad();

          return;

        } catch (error) {

          console.log(
            "Reconnect failed:",
            error
          );

        }

      }


      /* MEDIA ERROR */

      if (
        data.type ===
        window.Hls.ErrorTypes.MEDIA_ERROR
      ) {

        console.log(
          "HLS media error. Recovering..."
        );


        try {

          hls.recoverMediaError();

          return;

        } catch (error) {

          console.log(
            "Media recovery failed:",
            error
          );

        }

      }


      /* FATAL ERROR */

      showError(
        "The stream connection was lost."
      );

    }
  );


  return;

}


/* =======================================================
   HLS NOT SUPPORTED
   ======================================================= */

showError(
  "This browser does not support HLS playback."
);

}

/* =========================================================
RETRY
========================================================= */

function retryStream() {

hideError();

showLoading();


clearTimeout(
  retryTimer
);


retryTimer =
  setTimeout(
    function () {

      loadStream();

    },
    300
  );

}

/* =========================================================
TAP TO WATCH
========================================================= */

function startWatching() {

if (watchOverlay) {

  watchOverlay.classList.add(
    "hidden"
  );

}


showControlsForever();


video.play().catch(
  function (error) {

    console.log(
      "Playback requires user interaction:",
      error
    );

    updatePlayButton();

  }
);

}

/* =========================================================
EVENTS
========================================================= */

/* WATCH BUTTON */

if (watchButton) {

watchButton.addEventListener(
  "click",
  function (event) {

    event.stopPropagation();

    startWatching();

  }
);

}

/* PLAY BUTTON */

if (playButton) {

playButton.addEventListener(
  "click",
  function (event) {

    event.stopPropagation();

    togglePlay();

  }
);

}

/* CENTER PLAY */

if (centerPlay) {

centerPlay.addEventListener(
  "click",
  function (event) {

    event.stopPropagation();

    togglePlay();

  }
);

}

/* MUTE */

if (muteButton) {

muteButton.addEventListener(
  "click",
  function (event) {

    event.stopPropagation();

    toggleMute();

  }
);

}

/* VOLUME */

if (volumeSlider) {

volumeSlider.addEventListener(
  "input",
  updateVolume
);

}

/* FULLSCREEN */

if (fullscreenButton) {

fullscreenButton.addEventListener(
  "click",
  function (event) {

    event.stopPropagation();

    toggleFullscreen();

  }
);

}

/* QUALITY */

if (qualityButton) {

qualityButton.addEventListener(
  "click",
  function (event) {

    event.stopPropagation();


    if (qualityMenu) {

      qualityMenu.hidden =
        !qualityMenu.hidden;

    }


    showControlsForever();

  }
);

}

/* RETRY */

if (retryButton) {

retryButton.addEventListener(
  "click",
  function (event) {

    event.stopPropagation();

    retryStream();

  }
);

}

/* =========================================================
VIDEO EVENTS
========================================================= */

video.addEventListener(
"play",
function () {

  updatePlayButton();

  showControls();

}

);

video.addEventListener(
"pause",
function () {

  updatePlayButton();

  showControlsForever();

}

);

video.addEventListener(
"waiting",
function () {

  showLoading();

}

);

video.addEventListener(
"playing",
function () {

  hideLoading();

  hideError();

  updatePlayButton();

  showControls();

}

);

video.addEventListener(
"canplay",
function () {

  /*
   * If enough data is available,
   * don't leave the loading screen
   * stuck on screen.
   */

  if (!video.paused) {
    hideLoading();
  }

}

);

video.addEventListener(
"error",
function () {

  /*
   * HLS.js handles its own errors.
   * This catches native video errors.
   */

  if (!hls) {

    showError(
      "The stream could not be loaded."
    );

  }

}

);

/* VIDEO CLICK */

video.addEventListener(
"click",
function () {

  togglePlay();

  showControls();

}

);

/* =========================================================
PLAYER TOUCH / MOUSE
========================================================= */

player.addEventListener(
"mousemove",
function () {

  showControls();

}

);

player.addEventListener(
"touchstart",
function () {

  showControls();

},
{
  passive: true
}

);

/* =========================================================
CLOSE QUALITY MENU
========================================================= */

document.addEventListener(
"click",
function (event) {

  if (
    qualityMenu &&
    qualityButton &&
    !qualityMenu.contains(
      event.target
    ) &&
    event.target !==
    qualityButton
  ) {

    qualityMenu.hidden =
      true;

  }

}

);

/* =========================================================
INITIALIZATION
========================================================= */

video.volume =
1;

if (volumeSlider) {

volumeSlider.value =
  1;

}

updateMuteButton();

updatePlayButton();

showControlsForever();

loadStream();

})();
