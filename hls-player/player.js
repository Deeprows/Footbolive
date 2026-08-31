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

  const backwardButton =
    document.getElementById("backwardButton");

  const forwardButton =
    document.getElementById("forwardButton");

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
     SETTINGS
     ========================================================= */

  const SEEK_SECONDS = 10;


  /* =========================================================
     STREAM URL
     ========================================================= */

  function getStreamUrl() {

    /*
     * Stream format:
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


  /* =========================================================
     UI
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
     PLAY BUTTON
     ========================================================= */

  function updatePlayButton() {

    if (video.paused) {

      playButton.textContent =
        "▶";

      centerPlay.textContent =
        "▶";

      playButton.setAttribute(
        "aria-label",
        "Play"
      );

    } else {

      playButton.textContent =
        "❚❚";

      centerPlay.textContent =
        "❚❚";

      playButton.setAttribute(
        "aria-label",
        "Pause"
      );

    }

  }


  function togglePlay() {

    if (video.paused) {

      video.play().catch(function () {

        /*
         * Browser autoplay restrictions
         * are normal.
         */

      });

    } else {

      video.pause();

    }

  }


  /* =========================================================
     MUTE
     ========================================================= */

  function updateMuteButton() {

    if (
      video.muted ||
      video.volume === 0
    ) {

      muteButton.textContent =
        "🔇";

      muteButton.setAttribute(
        "aria-label",
        "Unmute"
      );

    } else {

      muteButton.textContent =
        "🔊";

      muteButton.setAttribute(
        "aria-label",
        "Mute"
      );

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

    const value =
      Number(
        volumeSlider.value
      );


    video.volume =
      value;


    if (value > 0) {
      video.muted = false;
    }


    updateMuteButton();

  }


  /* =========================================================
     SEEK / FORWARD / BACKWARD
     ========================================================= */

  function seekVideo(seconds) {

    /*
     * Make sure metadata exists.
     */

    if (
      !video ||
      video.readyState < 1
    ) {

      return;

    }


    /*
     * -------------------------------------------------------
     * NORMAL VIDEO / VOD
     * -------------------------------------------------------
     */

    if (
      Number.isFinite(video.duration)
    ) {

      const currentTime =
        video.currentTime;

      const targetTime =
        currentTime + seconds;


      const safeTime =
        Math.max(
          0,
          Math.min(
            targetTime,
            video.duration
          )
        );


      try {

        video.currentTime =
          safeTime;

      } catch (error) {

        console.log(
          "Seek error:",
          error
        );

      }


      return;

    }


    /*
     * -------------------------------------------------------
     * LIVE HLS / DVR
     * -------------------------------------------------------
     *
     * Live streams can have a seekable window.
     *
     * Example:
     *
     *  start = 1000 seconds
     *  end   = 1060 seconds
     *
     * We keep the seek inside that window.
     */

    if (
      video.seekable &&
      video.seekable.length > 0
    ) {

      try {

        const lastRange =
          video.seekable.length - 1;


        const start =
          video.seekable.start(
            lastRange
          );


        const end =
          video.seekable.end(
            lastRange
          );


        const current =
          video.currentTime;


        const target =
          current + seconds;


        const safeTarget =
          Math.max(
            start,
            Math.min(
              target,
              end
            )
          );


        video.currentTime =
          safeTarget;


      } catch (error) {

        console.log(
          "Live seek error:",
          error
        );

      }

      return;

    }


    /*
     * -------------------------------------------------------
     * NON-SEEKABLE LIVE STREAM
     * -------------------------------------------------------
     *
     * There is no DVR window.
     */

    console.log(
      "This live stream does not provide a seekable window."
    );

  }


  function seekBackward() {

    seekVideo(
      -SEEK_SECONDS
    );

    showControls();

  }


  function seekForward() {

    seekVideo(
      SEEK_SECONDS
    );

    showControls();

  }


  /* =========================================================
     FULLSCREEN
     ========================================================= */

  async function toggleFullscreen() {

    try {

      if (
        !document.fullscreenElement
      ) {

        if (
          player.requestFullscreen
        ) {

          await player.requestFullscreen();

        } else if (
          video.webkitEnterFullscreen
        ) {

          video.webkitEnterFullscreen();

        }

      } else {

        await document.exitFullscreen();

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
      .forEach(function (button) {

        button.remove();

      });

  }


  function addQualityButton(
    index,
    label
  ) {

    const button =
      document.createElement(
        "button"
      );


    button.type =
      "button";


    button.className =
      "quality-option";


    button.dataset.quality =
      String(index);


    button.textContent =
      label;


    qualityMenu.appendChild(
      button
    );

  }


  function buildQualityMenu() {

    if (!qualityMenu) {
      return;
    }


    clearQualityMenu();


    addQualityButton(
      -1,
      "Auto"
    );


    if (
      !hls ||
      !hls.levels ||
      !hls.levels.length
    ) {

      return;

    }


    hls.levels.forEach(
      function (
        level,
        index
      ) {

        const height =
          level.height;


        let label =
          height
            ? height + "p"
            : "Quality " +
              (index + 1);


        addQualityButton(
          index,
          label
        );

      }
    );


    qualityMenu
      .querySelectorAll(
        ".quality-option"
      )
      .forEach(function (button) {

        button.addEventListener(
          "click",
          function () {

            const quality =
              Number(
                button.dataset.quality
              );


            setQuality(
              quality
            );

          }
        );

      });

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


    qualityMenu.hidden =
      true;

  }


  /* =========================================================
     CONTROLS AUTO HIDE
     ========================================================= */

  let hideControlsTimer =
    null;


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

    if (hls) {

      try {

        hls.destroy();

      } catch (error) {

        console.log(
          error
        );

      }


      hls = null;

    }

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
     * =======================================================
     * NATIVE HLS
     * =======================================================
     */

    if (
      video.canPlayType(
        "application/vnd.apple.mpegurl"
      )
    ) {

      video.src =
        streamUrl;


      video.addEventListener(
        "loadedmetadata",
        function nativeReady() {

          hideLoading();

        },
        {
          once: true
        }
      );


      video.addEventListener(
        "error",
        function nativeError() {

          showError(
            "The stream could not be loaded."
          );

        },
        {
          once: true
        }
      );


      return;

    }


    /*
     * =======================================================
     * HLS.JS
     * =======================================================
     */

    if (
      window.Hls &&
      Hls.isSupported()
    ) {

      hls =
        new Hls({

          enableWorker:
            true,

          lowLatencyMode:
            true,

          /*
           * Keep enough buffer
           * for short backward seeking.
           */

          backBufferLength:
            30,

          liveSyncDurationCount:
            3,

          maxBufferLength:
            30,

          /*
           * Allow the player
           * to maintain a seekable
           * DVR window where
           * the stream provides one.
           */

          maxMaxBufferLength:
            60

        });


      hls.loadSource(
        streamUrl
      );


      hls.attachMedia(
        video
      );


      hls.on(
        Hls.Events.MANIFEST_PARSED,
        function () {

          hideLoading();


          buildQualityMenu();


          /*
           * Start with Auto quality.
           */

          hls.currentLevel =
            -1;

        }
      );


      hls.on(
        Hls.Events.LEVEL_SWITCHED,
        function () {

          /*
           * Keep quality menu
           * synchronized.
           */

          if (
            currentQuality === -1
          ) {

            const autoButton =
              qualityMenu.querySelector(
                '[data-quality="-1"]'
              );


            if (autoButton) {

              qualityMenu
                .querySelectorAll(
                  ".quality-option"
                )
                .forEach(
                  function (button) {

                    button.classList.remove(
                      "active"
                    );

                  }
                );


              autoButton.classList.add(
                "active"
              );

            }

          }

        }
      );


      hls.on(
        Hls.Events.ERROR,
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


          /*
           * NETWORK ERROR
           */

          if (
            data.type ===
            Hls.ErrorTypes.NETWORK_ERROR
          ) {

            try {

              hls.startLoad();

              return;

            } catch (error) {

              console.log(
                error
              );

            }

          }


          /*
           * MEDIA ERROR
           */

          if (
            data.type ===
            Hls.ErrorTypes.MEDIA_ERROR
          ) {

            try {

              hls.recoverMediaError();

              return;

            } catch (error) {

              console.log(
                error
              );

            }

          }


          showError(
            "The stream connection was lost."
          );

        }
      );


      return;

    }


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
        loadStream,
        300
      );

  }


  /* =========================================================
     WATCH
     ========================================================= */

  function startWatching() {

    watchOverlay.classList.add(
      "hidden"
    );


    showControlsForever();


    video.play().catch(
      function () {

        /*
         * If autoplay is blocked,
         * the user can press play.
         */

        updatePlayButton();

      }
    );

  }


  /* =========================================================
     EVENTS
     ========================================================= */

  watchButton.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();

      startWatching();

    }
  );


  /* PLAY */

  playButton.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();

      togglePlay();

    }
  );


  /* CENTER PLAY */

  centerPlay.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();

      togglePlay();

    }
  );


  /* =========================================================
     BACKWARD
     ========================================================= */

  backwardButton.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();

      seekBackward();

    }
  );


  /* =========================================================
     FORWARD
     ========================================================= */

  forwardButton.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();

      seekForward();

    }
  );


  /* =========================================================
     MUTE
     ========================================================= */

  muteButton.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();

      toggleMute();

    }
  );


  /* =========================================================
     VOLUME
     ========================================================= */

  volumeSlider.addEventListener(
    "input",
    updateVolume
  );


  /* =========================================================
     FULLSCREEN
     ========================================================= */

  fullscreenButton.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();

      toggleFullscreen();

    }
  );


  /* =========================================================
     QUALITY
     ========================================================= */

  qualityButton.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();


      qualityMenu.hidden =
        !qualityMenu.hidden;


      showControlsForever();

    }
  );


  /* =========================================================
     RETRY
     ========================================================= */

  retryButton.addEventListener(
    "click",
    function (event) {

      event.stopPropagation();

      retryStream();

    }
  );


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

      showControls();

    }
  );


  /* =========================================================
     VIDEO CLICK
     ========================================================= */

  video.addEventListener(
    "click",
    function () {

      togglePlay();

      showControls();

    }
  );


  /* =========================================================
     PLAYER MOUSE
     ========================================================= */

  player.addEventListener(
    "mousemove",
    function () {

      showControls();

    }
  );


  /* =========================================================
     PLAYER TOUCH
     ========================================================= */

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
     KEYBOARD SHORTCUTS
     ========================================================= */

  document.addEventListener(
    "keydown",
    function (event) {

      /*
       * Do not interfere with
       * text inputs.
       */

      if (
        event.target.tagName ===
          "INPUT" ||
        event.target.tagName ===
          "TEXTAREA"
      ) {

        return;

      }


      switch (event.key) {

        /*
         * Space
         */

        case " ":

          event.preventDefault();

          togglePlay();

          showControls();

          break;


        /*
         * Left arrow
         *
         * Back 10 seconds
         */

        case "ArrowLeft":

          event.preventDefault();

          seekBackward();

          break;


        /*
         * Right arrow
         *
         * Forward 10 seconds
         */

        case "ArrowRight":

          event.preventDefault();

          seekForward();

          break;


        /*
         * M
         *
         * Mute
         */

        case "m":
        case "M":

          toggleMute();

          break;

      }

    }
  );


  /* =========================================================
     CLOSE QUALITY MENU
     ========================================================= */

  document.addEventListener(
    "click",
    function (event) {

      if (
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


  volumeSlider.value =
    1;


  updateMuteButton();

  updatePlayButton();

  loadStream();


})();
