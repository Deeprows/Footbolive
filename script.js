document.addEventListener("DOMContentLoaded", function () {

  "use strict";

  console.log("Deeprowss app loaded");


  /* =========================================================
     ZOOM / GESTURE PROTECTION
     ========================================================= */

  document.addEventListener(
    "gesturestart",
    function (event) {
      event.preventDefault();
    },
    { passive: false }
  );

  document.addEventListener(
    "gesturechange",
    function (event) {
      event.preventDefault();
    },
    { passive: false }
  );

  document.addEventListener(
    "gestureend",
    function (event) {
      event.preventDefault();
    },
    { passive: false }
  );

  document.addEventListener(
    "wheel",
    function (event) {
      if (event.ctrlKey) {
        event.preventDefault();
      }
    },
    { passive: false }
  );

  document.addEventListener(
    "keydown",
    function (event) {

      if (!event.ctrlKey) {
        return;
      }

      const key = event.key;

      if (
        key === "+" ||
        key === "=" ||
        key === "-" ||
        key === "0"
      ) {
        event.preventDefault();
      }

    }
  );


  /* =========================================================
     ELEMENTS
     ========================================================= */

  const siteHeader =
    document.querySelector(".site-header");

  const screenSection =
    document.querySelector(".screen-section");

  const screenFrame =
    document.getElementById("screenFrame");

  const screenPlaceholder =
    document.getElementById("screenPlaceholder");

  const screenStatus =
    document.getElementById("screenStatus");

  const nowShowing =
    document.getElementById("nowShowing");

  const fullscreenButton =
    document.getElementById("fullscreenButton");

  const screenPlayer =
    document.getElementById("screenPlayer");


  /* =========================================================
     FOOTBALL SCREEN CONTROLS
     ========================================================= */

  const footballScreenControls =
    document.getElementById("footballScreenControls");

  const mainScreenButton =
    document.getElementById("mainScreenButton");

  const altScreenButton =
    document.getElementById("altScreenButton");


  /* =========================================================
     ALTERNATIVE FOOTBALL SCREEN
     ========================================================= */

  const altScreenOverlay =
    document.getElementById("altScreenOverlay");

  const altScreenFrame =
    document.getElementById("altScreenFrame");

  const altScreenMatch =
    document.getElementById("altScreenMatch");

  const closeAltScreen =
    document.getElementById("closeAltScreen");


  /* =========================================================
     APP BUTTONS
     ========================================================= */

  const footballButton =
    document.getElementById("footballButton");

  const highlightsButton =
    document.getElementById("highlightsButton");

  const tvButton =
    document.getElementById("tvButton");

  const moviesButton =
    document.getElementById("moviesButton");


  /* =========================================================
     CONTENT SECTIONS
     ========================================================= */

  const footballContent =
    document.getElementById("footballContent");

  const highlightsContent =
    document.getElementById("highlightsContent");

  const tvContent =
    document.getElementById("tvContent");

  const moviesContent =
    document.getElementById("moviesContent");


  /* =========================================================
     NAVIGATION
     ========================================================= */

  const navFootball =
    document.getElementById("navFootball");

  const navHighlights =
    document.getElementById("navHighlights");

  const navTV =
    document.getElementById("navTV");

  const navMovies =
    document.getElementById("navMovies");


  /* =========================================================
     MATCHES
     ========================================================= */

  const matchCards =
    document.querySelectorAll(".match-card");


  /* =========================================================
     HIGHLIGHTS
     ========================================================= */

  const highlightCards =
    document.querySelectorAll(".highlight-card");


  /* =========================================================
     TV CHANNELS
     ========================================================= */

  const tvChannels =
    document.querySelectorAll(".tv-channel");


  /* =========================================================
     MOVIES
     ========================================================= */

  const movieCards =
    document.querySelectorAll(".movie-card");


  /* =========================================================
     MOBILE MENU
     ========================================================= */

  const menuToggle =
    document.getElementById("menuToggle");

  const mainNav =
    document.getElementById("mainNav");


  /* =========================================================
     CURRENT SCREEN STATE
     ========================================================= */

  let currentScreenType = "";
  let currentMatchCard = null;
  let currentMainUrl = "";
  let currentAltUrl = "";
  let screenLoadTimer = null;


  /* =========================================================
     STICKY LAYOUT CALCULATION
     ========================================================= */

  function updateStickyPositions() {

    if (!screenSection) {
      return;
    }

    const root =
      document.documentElement;

    const headerHeight =
      siteHeader
        ? siteHeader.getBoundingClientRect().height
        : 66;

    const screenHeight =
      screenSection.getBoundingClientRect().height;

    root.style.setProperty(
      "--header-height",
      headerHeight + "px"
    );

    root.style.setProperty(
      "--screen-stack-height",
      screenHeight + "px"
    );

    root.style.setProperty(
      "--app-menu-top",
      (
        headerHeight +
        screenHeight
      ) + "px"
    );

  }


  updateStickyPositions();


  window.addEventListener(
    "resize",
    updateStickyPositions
  );


  if (
    "ResizeObserver" in window &&
    screenSection
  ) {

    const stickyObserver =
      new ResizeObserver(
        function () {
          updateStickyPositions();
        }
      );

    stickyObserver.observe(
      screenSection
    );

  }


  /* =========================================================
     ACTIVE APP BUTTON
     ========================================================= */

  function setActiveButton(button) {

    [
      footballButton,
      highlightsButton,
      tvButton,
      moviesButton
    ].forEach(
      function (item) {

        if (item) {
          item.classList.remove("active");
        }

      }
    );

    if (button) {
      button.classList.add("active");
    }

  }


  /* =========================================================
     ACTIVE NAV
     ========================================================= */

  function setActiveNav(link) {

    [
      navFootball,
      navHighlights,
      navTV,
      navMovies
    ].forEach(
      function (item) {

        if (item) {
          item.classList.remove("active");
        }

      }
    );

    if (link) {
      link.classList.add("active");
    }

  }


  /* =========================================================
     SHOW CONTENT
     ========================================================= */

  function showContent(section) {

    if (footballContent) {
      footballContent.hidden =
        section !== "football";
    }

    if (highlightsContent) {
      highlightsContent.hidden =
        section !== "highlights";
    }

    if (tvContent) {
      tvContent.hidden =
        section !== "tv";
    }

    if (moviesContent) {
      moviesContent.hidden =
        section !== "movies";
    }


    /*
     * Alt Screen is football-only.
     * Leaving Football automatically closes it.
     */

    if (section !== "football") {
      hideAltScreen();
    }


    requestAnimationFrame(
      updateStickyPositions
    );

  }


  /* =========================================================
     OPEN FOOTBALL
     ========================================================= */

  function openFootball() {

    showContent("football");

    setActiveButton(
      footballButton
    );

    setActiveNav(
      navFootball
    );


    /*
     * If a football match is already selected,
     * restore its Main / Alt controls.
     */

    updateFootballControls();

  }


  /* =========================================================
     OPEN HIGHLIGHTS
     ========================================================= */

  function openHighlights() {

    showContent("highlights");

    setActiveButton(
      highlightsButton
    );

    setActiveNav(
      navHighlights
    );

  }


  /* =========================================================
     OPEN TV
     ========================================================= */

  function openTV() {

    showContent("tv");

    setActiveButton(
      tvButton
    );

    setActiveNav(
      navTV
    );

  }


  /* =========================================================
     OPEN MOVIES
     ========================================================= */

  function openMovies() {

    showContent("movies");

    setActiveButton(
      moviesButton
    );

    setActiveNav(
      navMovies
    );

  }


  /* =========================================================
     APP BUTTON EVENTS
     ========================================================= */

  if (footballButton) {

    footballButton.addEventListener(
      "click",
      openFootball
    );

  }

  if (highlightsButton) {

    highlightsButton.addEventListener(
      "click",
      openHighlights
    );

  }

  if (tvButton) {

    tvButton.addEventListener(
      "click",
      openTV
    );

  }

  if (moviesButton) {

    moviesButton.addEventListener(
      "click",
      openMovies
    );

  }


  /* =========================================================
     NAVIGATION EVENTS
     ========================================================= */

  if (navFootball) {

    navFootball.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        openFootball();

      }
    );

  }

  if (navHighlights) {

    navHighlights.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        openHighlights();

      }
    );

  }

  if (navTV) {

    navTV.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        openTV();

      }
    );

  }

  if (navMovies) {

    navMovies.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        openMovies();

      }
    );

  }


  /* =========================================================
     CHECK ALT URL
     ========================================================= */

  function hasValidAltUrl(url) {

    if (!url) {
      return false;
    }

    const normalized =
      String(url).trim();

    if (!normalized) {
      return false;
    }

    if (
      normalized.indexOf("PASTE-") === 0 ||
      normalized.indexOf("ALT-SCREEN-URL") !== -1
    ) {
      return false;
    }

    return true;

  }


  /* =========================================================
     UPDATE FOOTBALL CONTROLS
     ========================================================= */

  function updateFootballControls() {

    if (!footballScreenControls) {
      return;
    }


    /*
     * Alt Screen exists ONLY when:
     *
     * 1. Current screen is a football match.
     * 2. The selected match has a valid data-alt-url.
     */

    const shouldShow =
      currentScreenType === "match" &&
      hasValidAltUrl(currentAltUrl);


    footballScreenControls.hidden =
      !shouldShow;


    if (!shouldShow) {

      if (mainScreenButton) {
        mainScreenButton.classList.remove("active");
      }

      if (altScreenButton) {
        altScreenButton.classList.remove("active");
      }

      return;

    }


    if (mainScreenButton) {
      mainScreenButton.classList.add("active");
    }

    if (altScreenButton) {
      altScreenButton.classList.remove("active");
    }

  }


  /* =========================================================
     HIDE ALT SCREEN
     ========================================================= */

  function hideAltScreen() {

    if (altScreenFrame) {
      altScreenFrame.src =
        "about:blank";
    }


    if (altScreenOverlay) {
      altScreenOverlay.hidden =
        true;
    }


    document.body.classList.remove(
      "alt-screen-open"
    );


    if (mainScreenButton) {
      mainScreenButton.classList.add("active");
    }

    if (altScreenButton) {
      altScreenButton.classList.remove("active");
    }


    /*
     * Never leave Alt Screen active after switching
     * to another section or another match.
     */

  }


  /* =========================================================
     OPEN ALT SCREEN
     ========================================================= */

  function openAltScreen() {

    /*
     * Alt Screen can ONLY be opened for Football.
     */

    if (
      currentScreenType !== "match" ||
      !currentMatchCard
    ) {

      return;

    }


    if (!hasValidAltUrl(currentAltUrl)) {

      alert(
        "No alternative screen has been added for this match yet."
      );

      return;

    }


    const matchName =
      currentMatchCard.dataset.name ||
      currentMatchCard.querySelector(
        ".match-teams"
      )?.textContent?.trim() ||
      "Football Match";


    if (altScreenMatch) {
      altScreenMatch.textContent =
        matchName;
    }


    if (altScreenFrame) {

      altScreenFrame.src =
        "about:blank";


      /*
       * Small delay prevents old content from visually
       * flashing before the alternative stream loads.
       */

      setTimeout(
        function () {

          if (
            currentScreenType === "match" &&
            hasValidAltUrl(currentAltUrl)
          ) {

            altScreenFrame.src =
              currentAltUrl;

          }

        },
        80
      );

    }


    if (altScreenOverlay) {
      altScreenOverlay.hidden =
        false;
    }


    document.body.classList.add(
      "alt-screen-open"
    );


    if (mainScreenButton) {
      mainScreenButton.classList.remove("active");
    }

    if (altScreenButton) {
      altScreenButton.classList.add("active");
    }

  }


  /* =========================================================
     MAIN SCREEN BUTTON
     ========================================================= */

  if (mainScreenButton) {

    mainScreenButton.addEventListener(
      "click",
      function () {

        if (
          currentScreenType !== "match" ||
          !currentMainUrl
        ) {
          return;
        }


        hideAltScreen();

        loadScreen(
          currentMainUrl,
          currentMatchCard?.dataset.name ||
          "Football Match",
          "match",
          true
        );

      }
    );

  }


  /* =========================================================
     ALT SCREEN BUTTON
     ========================================================= */

  if (altScreenButton) {

    altScreenButton.addEventListener(
      "click",
      function () {
        openAltScreen();
      }
    );

  }


  /* =========================================================
     CLOSE ALT SCREEN BUTTON
     ========================================================= */

  if (closeAltScreen) {

    closeAltScreen.addEventListener(
      "click",
      function () {
        hideAltScreen();
      }
    );

  }


  /* =========================================================
     CLICK OUTSIDE ALT SCREEN
     ========================================================= */

  if (altScreenOverlay) {

    altScreenOverlay.addEventListener(
      "click",
      function (event) {

        if (
          event.target === altScreenOverlay
        ) {

          hideAltScreen();

        }

      }
    );

  }


  /* =========================================================
     ESC KEY
     ========================================================= */

  document.addEventListener(
    "keydown",
    function (event) {

      if (
        event.key === "Escape" &&
        altScreenOverlay &&
        !altScreenOverlay.hidden
      ) {

        hideAltScreen();

      }

    }
  );


  /* =========================================================
     LOAD SCREEN
     ========================================================= */

  function loadScreen(
    url,
    name,
    type,
    keepMatchState
  ) {

    if (!screenFrame) {
      return;
    }

    if (!url) {
      return;
    }


    /*
     * Cancel a previous delayed iframe load.
     */

    if (screenLoadTimer) {

      clearTimeout(
        screenLoadTimer
      );

      screenLoadTimer = null;

    }


    /*
     * Alt Screen is always closed when the main screen,
     * TV, Movie, or Highlight changes.
     */

    hideAltScreen();


    currentScreenType =
      type || "";


    if (!keepMatchState) {

      if (type !== "match") {

        currentMatchCard = null;
        currentMainUrl = "";
        currentAltUrl = "";

      }

    }


    if (nowShowing) {

      nowShowing.textContent =
        name || "Now Playing";

    }


    if (screenPlaceholder) {

      screenPlaceholder.classList.add(
        "hidden"
      );

    }


    screenFrame.style.opacity =
      "0.25";


    screenFrame.src =
      "about:blank";


    screenLoadTimer =
      setTimeout(
        function () {

          screenFrame.src =
            url;

          screenFrame.style.opacity =
            "1";

          screenLoadTimer = null;


          requestAnimationFrame(
            updateStickyPositions
          );

        },
        150
      );


    /* =======================================================
       SCREEN STATUS
       ======================================================= */

    if (screenStatus) {

      if (type === "tv") {

        screenStatus.textContent =
          "LIVE TV";

      }

      else if (type === "highlight") {

        screenStatus.textContent =
          "HIGHLIGHT";

      }

      else if (type === "movie") {

        screenStatus.textContent =
          "MOVIE";

      }

      else {

        screenStatus.textContent =
          "LIVE";

      }

    }


    /*
     * Football controls are recalculated after every screen
     * change. TV, Movies and Highlights cannot display them.
     */

    updateFootballControls();


    /*
     * Keep player visible.
     */

    if (screenPlayer) {

      const rect =
        screenPlayer.getBoundingClientRect();

      const headerHeight =
        siteHeader
          ? siteHeader.getBoundingClientRect().height
          : 66;


      if (
        rect.top <
        headerHeight
      ) {

        window.scrollBy({
          top:
            rect.top -
            headerHeight -
            10,

          behavior:
            "smooth"
        });

      }

    }

  }


  /* =========================================================
     MATCH CLICK
     ========================================================= */

  matchCards.forEach(
    function (card) {

      card.addEventListener(
        "click",
        function () {

          const url =
            this.dataset.url;


          const altUrl =
            this.dataset.altUrl || "";


          const name =
            this.dataset.name ||
            this.querySelector(
              ".match-teams"
            )?.textContent?.trim() ||
            "Football Match";


          /*
           * Save the selected football match.
           */

          currentMatchCard =
            this;

          currentMainUrl =
            url || "";

          currentAltUrl =
            altUrl || "";

          currentScreenType =
            "match";


          /*
           * Mark selected match.
           */

          matchCards.forEach(
            function (item) {

              item.classList.remove(
                "active",
                "selected"
              );

            }
          );


          this.classList.add(
            "active",
            "selected"
          );


          /*
           * Always return to Main Screen when a new
           * football match is selected.
           */

          hideAltScreen();


          loadScreen(
            url,
            name,
            "match",
            true
          );


          /*
           * Make sure Football section is active.
           */

          openFootball();

        }
      );

    }
  );


  /* =========================================================
     HIGHLIGHT CLICK
     ========================================================= */

  highlightCards.forEach(
    function (card) {

      card.addEventListener(
        "click",
        function () {

          const url =
            this.dataset.url;


          const name =
            this.dataset.name ||
            this.querySelector(
              "strong"
            )?.textContent?.trim() ||
            "Football Highlight";


          highlightCards.forEach(
            function (item) {

              item.classList.remove(
                "active"
              );

            }
          );


          this.classList.add(
            "active"
          );


          currentMatchCard =
            null;

          currentMainUrl =
            "";

          currentAltUrl =
            "";

          loadScreen(
            url,
            name,
            "highlight"
          );


          openHighlights();

        }
      );

    }
  );


  /* =========================================================
     TV CHANNEL CLICK
     ========================================================= */

  tvChannels.forEach(
    function (channel) {

      channel.addEventListener(
        "click",
        function () {

          const url =
            this.dataset.url;


          const name =
            this.dataset.name ||
            this.querySelector(
              ".tv-channel-info strong"
            )?.textContent?.trim() ||
            "TV Channel";


          tvChannels.forEach(
            function (item) {

              item.classList.remove(
                "active"
              );

            }
          );


          this.classList.add(
            "active"
          );


          currentMatchCard =
            null;

          currentMainUrl =
            "";

          currentAltUrl =
            "";

          loadScreen(
            url,
            name,
            "tv"
          );


          openTV();

        }
      );

    }
  );


  /* =========================================================
     MOVIE CLICK
     ========================================================= */

  movieCards.forEach(
    function (movie) {

      movie.addEventListener(
        "click",
        function () {

          const url =
            this.dataset.url;


          const name =
            this.dataset.name ||
            this.querySelector(
              ".movie-title"
            )?.textContent?.trim() ||
            "Movie";


          movieCards.forEach(
            function (item) {

              item.classList.remove(
                "active"
              );

            }
          );


          this.classList.add(
            "active"
          );


          currentMatchCard =
            null;

          currentMainUrl =
            "";

          currentAltUrl =
            "";

          loadScreen(
            url,
            name,
            "movie"
          );


          openMovies();

        }
      );

    }
  );


  /* =========================================================
     FULLSCREEN
     ========================================================= */

  function enterScreenFullscreen() {

    if (!screenPlayer) {
      return;
    }


    screenPlayer.classList.add(
      "fullscreen-active"
    );


    const requestFullscreen =
      screenPlayer.requestFullscreen ||
      screenPlayer.webkitRequestFullscreen ||
      screenPlayer.mozRequestFullScreen ||
      screenPlayer.msRequestFullscreen;


    if (!requestFullscreen) {

      screenPlayer.classList.remove(
        "fullscreen-active"
      );


      console.log(
        "Fullscreen API is not available."
      );


      return;

    }


    try {

      const result =
        requestFullscreen.call(
          screenPlayer,
          {
            navigationUI: "hide"
          }
        );


      if (
        result &&
        typeof result.then === "function"
      ) {

        result.catch(
          function (error) {

            console.log(
              "Fullscreen request failed:",
              error
            );


            screenPlayer.classList.remove(
              "fullscreen-active"
            );

          }
        );

      }

    }

    catch (error) {

      console.log(
        "Fullscreen request failed:",
        error
      );


      screenPlayer.classList.remove(
        "fullscreen-active"
      );

    }

  }


  function exitScreenFullscreen() {

    if (
      screen.orientation &&
      screen.orientation.unlock
    ) {

      try {
        screen.orientation.unlock();
      }

      catch (error) {

        console.log(
          "Orientation unlock unavailable:",
          error
        );

      }

    }


    const exitFullscreen =
      document.exitFullscreen ||
      document.webkitExitFullscreen ||
      document.mozCancelFullScreen ||
      document.msExitFullscreen;


    if (!exitFullscreen) {

      if (screenPlayer) {

        screenPlayer.classList.remove(
          "fullscreen-active"
        );

      }

      return;

    }


    try {

      const result =
        exitFullscreen.call(
          document
        );


      if (
        result &&
        typeof result.then === "function"
      ) {

        result.catch(
          function (error) {

            console.log(
              "Fullscreen exit failed:",
              error
            );

          }
        );

      }

    }

    catch (error) {

      console.log(
        "Fullscreen exit failed:",
        error
      );

    }

  }


  if (fullscreenButton) {

    fullscreenButton.addEventListener(
      "click",
      function () {

        const activeFullscreen =
          document.fullscreenElement ||
          document.webkitFullscreenElement ||
          document.mozFullScreenElement ||
          document.msFullscreenElement;


        if (activeFullscreen) {

          exitScreenFullscreen();

          return;

        }


        enterScreenFullscreen();

      }
    );

  }


  /* =========================================================
     FULLSCREEN CHANGE
     ========================================================= */

  function handleFullscreenChange() {

    if (!fullscreenButton) {
      return;
    }


    const activeFullscreen =
      document.fullscreenElement ||
      document.webkitFullscreenElement ||
      document.mozFullScreenElement ||
      document.msFullscreenElement;


    if (activeFullscreen) {

      if (screenPlayer) {

        screenPlayer.classList.add(
          "fullscreen-active"
        );

      }


      fullscreenButton.textContent =
        "×";


      fullscreenButton.setAttribute(
        "aria-label",
        "Exit fullscreen"
      );


      fullscreenButton.setAttribute(
        "title",
        "Exit fullscreen"
      );


      if (
        screen.orientation &&
        screen.orientation.lock
      ) {

        try {

          const lockResult =
            screen.orientation.lock(
              "landscape"
            );


          if (
            lockResult &&
            typeof lockResult.catch === "function"
          ) {

            lockResult.catch(
              function (error) {

                console.log(
                  "Landscape lock unavailable:",
                  error
                );

              }
            );

          }

        }

        catch (error) {

          console.log(
            "Landscape lock unavailable:",
            error
          );

        }

      }

    }

    else {

      if (screenPlayer) {

        screenPlayer.classList.remove(
          "fullscreen-active"
        );

      }


      fullscreenButton.textContent =
        "⛶";


      fullscreenButton.setAttribute(
        "aria-label",
        "Enter fullscreen"
      );


      fullscreenButton.setAttribute(
        "title",
        "Enter fullscreen"
      );


      if (
        screen.orientation &&
        screen.orientation.unlock
      ) {

        try {
          screen.orientation.unlock();
        }

        catch (error) {

          console.log(
            "Orientation unlock unavailable:",
            error
          );

        }

      }

    }


    requestAnimationFrame(
      function () {
        updateStickyPositions();
      }
    );

  }


  document.addEventListener(
    "fullscreenchange",
    handleFullscreenChange
  );

  document.addEventListener(
    "webkitfullscreenchange",
    handleFullscreenChange
  );

  document.addEventListener(
    "mozfullscreenchange",
    handleFullscreenChange
  );

  document.addEventListener(
    "MSFullscreenChange",
    handleFullscreenChange
  );


  /* =========================================================
     FULLSCREEN / ORIENTATION RESIZE
     ========================================================= */

  let fullscreenResizeTimer =
    null;


  function handleFullscreenResize() {

    if (fullscreenResizeTimer) {

      clearTimeout(
        fullscreenResizeTimer
      );

    }


    fullscreenResizeTimer =
      setTimeout(
        function () {

          requestAnimationFrame(
            function () {

              updateStickyPositions();

            }
          );

        },
        80
      );

  }


  window.addEventListener(
    "resize",
    handleFullscreenResize
  );


  if (
    screen.orientation &&
    screen.orientation.addEventListener
  ) {

    screen.orientation.addEventListener(
      "change",
      handleFullscreenResize
    );

  }


  /* =========================================================
     MOBILE MENU
     ========================================================= */

  if (
    menuToggle &&
    mainNav
  ) {

    menuToggle.addEventListener(
      "click",
      function () {

        const isOpen =
          mainNav.classList.toggle(
            "open"
          );


        menuToggle.setAttribute(
          "aria-expanded",
          isOpen
            ? "true"
            : "false"
        );

      }
    );


    mainNav
      .querySelectorAll("a")
      .forEach(
        function (link) {

          link.addEventListener(
            "click",
            function () {

              mainNav.classList.remove(
                "open"
              );


              menuToggle.setAttribute(
                "aria-expanded",
                "false"
              );

            }
          );

        }
      );

  }


  /* =========================================================
     ALT SCREEN PAGE SAFETY
     ========================================================= */

  window.addEventListener(
    "pagehide",
    function () {

      hideAltScreen();


      if (screenPlayer) {

        screenPlayer.classList.remove(
          "fullscreen-active"
        );

      }

    }
  );


  /* =========================================================
     INITIAL STATE
     ========================================================= */

  openFootball();


  requestAnimationFrame(
    function () {
      updateStickyPositions();
    }
  );


});
