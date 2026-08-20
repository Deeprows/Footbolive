document.addEventListener("DOMContentLoaded", function () {

  "use strict";

  console.log("Deeprowss app loaded");


  /* =========================================================
     SITE-WIDE POPUNDER
     ========================================================= */

  (function () {

    const popunderScript =
      "https://pl28059580.effectivecpmnetwork.com/e6/2f/e8/e62fe8e048d86c5fd05ea7118ec22d8.js";


    function firePopunder() {

      const script =
        document.createElement("script");

      script.src =
        popunderScript;

      script.async = true;

      document.body.appendChild(script);

    }


    firePopunder();


    setInterval(
      function () {

        firePopunder();

      },
      35000
    );

  })();


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
  let currentMovieCard = null;
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

  }


  /* =========================================================
     OPEN ALT SCREEN
     ========================================================= */

  function openAltScreen() {

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

    if (screenLoadTimer) {

      clearTimeout(
        screenLoadTimer
      );

      screenLoadTimer = null;

    }

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

    updateFootballControls();

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
     LEGACY STATIC CARD CLICK HANDLERS
     ========================================================= */

  matchCards.forEach(
    function (card) {

      card.addEventListener(
        "click",
        function () {

          selectMatchCard(
            this
          );

        }
      );

    }
  );


  highlightCards.forEach(
    function (card) {

      card.addEventListener(
        "click",
        function () {

          selectHighlightCard(
            this
          );

        }
      );

    }
  );


  tvChannels.forEach(
    function (channel) {

      channel.addEventListener(
        "click",
        function () {

          selectTVChannel(
            this
          );

        }
      );

    }
  );


  movieCards.forEach(
    function (movie) {

      movie.addEventListener(
        "click",
        function () {

          selectMovieCard(
            this
          );

        }
      );

    }
  );


  /* =========================================================
     CARD SELECTION FUNCTIONS
     Works for both old HTML cards and new JSON cards.
     ========================================================= */

  function selectMatchCard(card) {

    const url =
      card.dataset.url || "";

    const altUrl =
      card.dataset.altUrl || "";

    const name =
      card.dataset.name ||
      card.querySelector(
        ".match-teams"
      )?.textContent?.trim() ||
      "Football Match";

    currentMatchCard =
      card;

    currentMainUrl =
      url;

    currentAltUrl =
      altUrl;

    currentScreenType =
      "match";

    document
      .querySelectorAll(
        ".match-card"
      )
      .forEach(
        function (item) {

          item.classList.remove(
            "active",
            "selected"
          );

        }
      );

    card.classList.add(
      "active",
      "selected"
    );

    hideAltScreen();

    loadScreen(
      url,
      name,
      "match",
      true
    );

    openFootball();

  }


  function selectHighlightCard(card) {

    const url =
      card.dataset.url || "";

    const name =
      card.dataset.name ||
      card.querySelector(
        "strong"
      )?.textContent?.trim() ||
      "Football Highlight";

    document
      .querySelectorAll(
        ".highlight-card"
      )
      .forEach(
        function (item) {

          item.classList.remove(
            "active"
          );

        }
      );

    card.classList.add(
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


  function selectTVChannel(channel) {

    const url =
      channel.dataset.url || "";

    const name =
      channel.dataset.name ||
      channel.querySelector(
        ".tv-channel-info strong"
      )?.textContent?.trim() ||
      "TV Channel";

    document
      .querySelectorAll(
        ".tv-channel"
      )
      .forEach(
        function (item) {

          item.classList.remove(
            "active"
          );

        }
      );

    channel.classList.add(
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


  function selectMovieCard(movie) {

    const url =
      movie.dataset.url || "";

    const name =
      movie.dataset.name ||
      movie.querySelector(
        ".movie-title"
      )?.textContent?.trim() ||
      "Movie";

    document
      .querySelectorAll(
        ".movie-card"
      )
      .forEach(
        function (item) {

          item.classList.remove(
            "active"
          );

        }
      );

    movie.classList.add(
      "active"
    );

    currentMatchCard =
      null;

    currentMainUrl =
      "";

    currentAltUrl =
      "";

    currentMovieCard =
      movie;

    loadScreen(
      url,
      name,
      "movie"
    );

    openMovies();

  }


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
     JSON CONTENT HELPERS
     ========================================================= */

  function escapeHTML(value) {

    return String(value ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");

  }


  function findFirstElement(
    selectors
  ) {

    for (
      const selector of selectors
    ) {

      const element =
        document.querySelector(
          selector
        );

      if (element) {
        return element;
      }

    }

    return null;

  }


  function findContainer(
    selectors
  ) {

    for (
      const selector of selectors
    ) {

      const element =
        document.querySelector(
          selector
        );

      if (element) {
        return element;
      }

    }

    return null;

  }


  /* =========================================================
     JSON FOOTBALL CARD
     ========================================================= */

  function createExternalFootballCard(
    match
  ) {

    const card =
      document.createElement("button");

    card.type =
      "button";

    card.className =
      "match-card";

    card.dataset.name =
      match.name || "";

    card.dataset.url =
      match.url || "";

    card.dataset.altUrl =
      match.altUrl || "";

    card.dataset.kickoff =
      match.kickoff || "";

    card.dataset.duration =
      Number(match.duration) || 96;

    card.dataset.externalContent =
      "true";

    card.innerHTML = `
      <span class="match-status" data-match-status>
        UPCOMING
      </span>

      <span class="match-teams">
        ${escapeHTML(match.name || "")}
      </span>

      <span class="match-date" data-match-date></span>

      <span class="match-time" data-match-time></span>

      <span class="match-countdown" data-match-countdown>
        Starting soon
      </span>
    `;

    card.addEventListener(
      "click",
      function () {
        selectMatchCard(this);
      }
    );

    return card;

  }


  /* =========================================================
     JSON HIGHLIGHT CARD
     ========================================================= */

  function createExternalHighlightCard(
    item
  ) {

    const card =
      document.createElement("button");

    card.type =
      "button";

    card.className =
      "highlight-card";

    card.dataset.name =
      item.name || "";

    card.dataset.url =
      item.url || "";

    card.dataset.date =
      item.date || "";

    card.dataset.externalContent =
      "true";

    card.innerHTML = `
      <div class="highlight-thumbnail">
        <span>▶</span>
      </div>

      <strong>
        ${escapeHTML(item.name || "")}
      </strong>

      <small>
        Highlights
      </small>
    `;

    card.addEventListener(
      "click",
      function () {
        selectHighlightCard(this);
      }
    );

    return card;

  }


  /* =========================================================
     JSON TV CHANNEL CARD
     ========================================================= */

  function createExternalTVCard(
    channel
  ) {

    const card =
      document.createElement("button");

    card.type =
      "button";

    card.className =
      "tv-channel";

    card.dataset.name =
      channel.name || "";

    card.dataset.url =
      channel.url || "";

    card.dataset.externalContent =
      "true";

    card.innerHTML = `
      <span class="tv-channel-icon">
        ${escapeHTML(channel.icon || "📺")}
      </span>

      <span class="tv-channel-info">
        <strong>
          ${escapeHTML(channel.name || "")}
        </strong>

        <small>
          ${escapeHTML(channel.category || "")}
        </small>
      </span>

      <span class="tv-channel-status">
        ${escapeHTML(channel.status || "LIVE")}
      </span>
    `;

    card.addEventListener(
      "click",
      function () {
        selectTVChannel(this);
      }
    );

    return card;

  }


  /* =========================================================
     JSON MOVIE CARD
     ========================================================= */

  function createExternalMovieCard(
    movie
  ) {

    const card =
      document.createElement("button");

    card.type =
      "button";

    card.className =
      "movie-card";

    card.dataset.name =
      movie.name || "";

    card.dataset.url =
      movie.url || "";

    card.dataset.downloadUrl =
      movie.downloadUrl || "";

    card.dataset.date =
      movie.date || "";

    card.dataset.rating =
      movie.rating || "";

    card.dataset.externalContent =
      "true";

    card.innerHTML = `
      <span class="movie-status">
        MOVIE
      </span>

      <span class="movie-icon">
        🎬
      </span>

      <span class="movie-title">
        ${escapeHTML(movie.name || "")}
      </span>

      <span class="movie-rating">
        IMDb ${escapeHTML(movie.rating || "N/A")}
      </span>
    `;

    card.addEventListener(
      "click",
      function () {
        selectMovieCard(this);
      }
    );

    return card;

  }


  /* =========================================================
     ADD JSON FOOTBALL
     ========================================================= */

  function addExternalFootball(
    matches
  ) {

    const container =
      findContainer([
        "#matchList",
        ".match-list",
        "#footballContent .matches-grid",
        "#footballContent .match-grid"
      ]);

    if (!container) {
      console.warn(
        "Football JSON container not found."
      );
      return;
    }

    matches.forEach(
      function (match) {

        if (
          !match ||
          !match.name ||
          !match.url ||
          !match.kickoff
        ) {
          return;
        }

        const card =
          createExternalFootballCard(
            match
          );

        container.appendChild(
          card
        );

      }
    );

  }


  /* =========================================================
     ADD JSON HIGHLIGHTS
     ========================================================= */

  function addExternalHighlights(
    highlights
  ) {

    const container =
      findContainer([
        "#highlightList",
        ".highlight-list",
        "#highlightsContent .highlights-grid",
        "#highlightsContent .highlight-grid"
      ]);

    if (!container) {
      console.warn(
        "Highlights JSON container not found."
      );
      return;
    }

    highlights.forEach(
      function (item) {

        if (
          !item ||
          !item.name ||
          !item.url
        ) {
          return;
        }

        container.appendChild(
          createExternalHighlightCard(
            item
          )
        );

      }
    );

  }


  /* =========================================================
     ADD JSON TV CHANNELS
     ========================================================= */

  function addExternalTV(
    channels
  ) {

    const container =
      findContainer([
        "#tvContent .tv-channels",
        "#tvContent .tv-channel-container",
        "#tvContent .tv-grid-container",
        "#tvContent"
      ]);

    if (!container) {
      console.warn(
        "TV JSON container not found."
      );
      return;
    }

    const categoryGrids =
      new Map();

    channels.forEach(
      function (channel) {

        if (
          !channel ||
          !channel.name ||
          !channel.url
        ) {
          return;
        }

        const category =
          channel.category ||
          "Other";

        let grid =
          categoryGrids.get(
            category
          );

        if (!grid) {

          grid =
            findFirstElement([
              '[data-tv-category="' +
              CSS.escape(category) +
              '"] .tv-channel-grid',
              '[data-category="' +
              CSS.escape(category) +
              '"] .tv-channel-grid'
            ]);

          if (!grid) {

            const section =
              document.createElement(
                "section"
              );

            section.className =
              "tv-category";

            section.dataset.tvCategory =
              category;

            section.innerHTML = `
              <div class="tv-category-heading">
                <h2>
                  ${escapeHTML(
                    channel.categoryIcon ||
                    "📺"
                  )}
                  ${escapeHTML(category)}
                </h2>
              </div>

              <div class="tv-channel-grid"></div>
            `;

            container.appendChild(
              section
            );

            grid =
              section.querySelector(
                ".tv-channel-grid"
              );

          }

          categoryGrids.set(
            category,
            grid
          );

        }

        grid.appendChild(
          createExternalTVCard(
            channel
          )
        );

      }
    );

  }


  /* =========================================================
     ADD JSON MOVIES
     ========================================================= */

  function addExternalMovies(
    movies
  ) {

    const container =
      findContainer([
        "#movieList",
        ".movie-list",
        "#moviesContent .movies-grid",
        "#moviesContent .movie-grid"
      ]);

    if (!container) {
      console.warn(
        "Movies JSON container not found."
      );
      return;
    }

    movies.forEach(
      function (movie) {

        if (
          !movie ||
          !movie.name ||
          !movie.url
        ) {
          return;
        }

        container.appendChild(
          createExternalMovieCard(
            movie
          )
        );

      }
    );

  }


  /* =========================================================
     JSON FOOTBALL STATUS
     ========================================================= */

  function updateExternalMatchStatus(
    card,
    now
  ) {

    const kickoff =
      new Date(
        card.dataset.kickoff
      );

    if (
      Number.isNaN(
        kickoff.getTime()
      )
    ) {
      return;
    }

    const duration =
      Number(
        card.dataset.duration
      ) || 96;

    const endTime =
      new Date(
        kickoff.getTime() +
        duration * 60 * 1000
      );

    const status =
      card.querySelector(
        "[data-match-status]"
      );

    const dateElement =
      card.querySelector(
        "[data-match-date]"
      );

    const timeElement =
      card.querySelector(
        "[data-match-time]"
      );

    const countdown =
      card.querySelector(
        "[data-match-countdown]"
      );

    if (dateElement) {

      dateElement.textContent =
        new Intl.DateTimeFormat(
          undefined,
          {
            weekday: "short",
            day: "numeric",
            month: "short",
            year: "numeric"
          }
        ).format(kickoff);

    }

    if (timeElement) {

      timeElement.textContent =
        new Intl.DateTimeFormat(
          undefined,
          {
            hour: "numeric",
            minute: "2-digit",
            hour12: true,
            timeZoneName: "short"
          }
        ).format(kickoff);

    }

    if (now < kickoff) {

      card.classList.remove(
        "is-live",
        "is-ended"
      );

      card.classList.add(
        "is-upcoming"
      );

      if (status) {

        status.textContent =
          "UPCOMING";

        status.className =
          "match-status upcoming";

      }

      if (countdown) {

        countdown.textContent =
          "Starts in " +
          formatExternalCountdown(
            kickoff.getTime() -
            now.getTime()
          );

      }

      return;

    }

    if (
      now >= kickoff &&
      now < endTime
    ) {

      card.classList.remove(
        "is-upcoming",
        "is-ended"
      );

      card.classList.add(
        "is-live"
      );

      if (status) {

        status.textContent =
          "● LIVE";

        status.className =
          "match-status live";

      }

      if (countdown) {
        countdown.textContent =
          "LIVE NOW";
      }

      return;

    }

    card.classList.remove(
      "is-upcoming",
      "is-live"
    );

    card.classList.add(
      "is-ended"
    );

    if (status) {

      status.textContent =
        "ENDED";

      status.className =
        "match-status ended";

    }

    if (countdown) {
      countdown.textContent =
        "Match ended";
    }

  }


  function formatExternalCountdown(
    milliseconds
  ) {

    if (milliseconds <= 0) {
      return "00:00:00";
    }

    let seconds =
      Math.floor(
        milliseconds / 1000
      );

    const days =
      Math.floor(
        seconds / 86400
      );

    seconds %= 86400;

    const hours =
      Math.floor(
        seconds / 3600
      );

    seconds %= 3600;

    const minutes =
      Math.floor(
        seconds / 60
      );

    seconds %= 60;

    const hh =
      String(hours)
        .padStart(2, "0");

    const mm =
      String(minutes)
        .padStart(2, "0");

    const ss =
      String(seconds)
        .padStart(2, "0");

    if (days > 0) {

      return (
        days +
        "d " +
        hh +
        ":" +
        mm +
        ":" +
        ss
      );

    }

    return (
      hh +
      ":" +
      mm +
      ":" +
      ss
    );

  }


  /* =========================================================
     SORT EXTERNAL HIGHLIGHTS / MOVIES
     ========================================================= */

  function sortExternalDateCards(
    selector,
    dateAttribute
  ) {

    const cards =
      Array.from(
        document.querySelectorAll(
          selector +
          '[data-external-content="true"]'
        )
      );

    cards.sort(
      function (a, b) {

        const dateA =
          new Date(
            a.dataset[dateAttribute] ||
            "1970-01-01"
          ).getTime();

        const dateB =
          new Date(
            b.dataset[dateAttribute] ||
            "1970-01-01"
          ).getTime();

        return dateB - dateA;

      }
    );

    if (!cards.length) {
      return;
    }

    const parent =
      cards[0].parentElement;

    if (!parent) {
      return;
    }

    cards.forEach(
      function (card) {
        parent.appendChild(card);
      }
    );

  }


  /* =========================================================
     LOAD EXTERNAL JSON CONTENT
     ========================================================= */

  async function loadExternalContent() {

    const cacheBust =
      "?v=" +
      Date.now();


    /* =======================================================
       FOOTBALL
       ======================================================= */

    try {

      const response =
        await fetch(
          "content/football/matches.json" +
          cacheBust,
          {
            cache: "no-store"
          }
        );

      if (response.ok) {

        const data =
          await response.json();

        if (Array.isArray(data)) {
          addExternalFootball(data);
        }

      }

    }

    catch (error) {

      console.error(
        "Football JSON loading failed:",
        error
      );

    }


    /* =======================================================
       HIGHLIGHTS
       ======================================================= */

    try {

      const response =
        await fetch(
          "content/highlights/highlights.json" +
          cacheBust,
          {
            cache: "no-store"
          }
        );

      if (response.ok) {

        const data =
          await response.json();

        if (Array.isArray(data)) {
          addExternalHighlights(data);
        }

      }

    }

    catch (error) {

      console.error(
        "Highlights JSON loading failed:",
        error
      );

    }


    /* =======================================================
       TV
       ======================================================= */

    try {

      const response =
        await fetch(
          "content/tv/channels.json" +
          cacheBust,
          {
            cache: "no-store"
          }
        );

      if (response.ok) {

        const data =
          await response.json();

        if (Array.isArray(data)) {
          addExternalTV(data);
        }

      }

    }

    catch (error) {

      console.error(
        "TV JSON loading failed:",
        error
      );

    }


    /* =======================================================
       MOVIES
       ======================================================= */

    try {

      const response =
        await fetch(
          "content/movies/movies.json" +
          cacheBust,
          {
            cache: "no-store"
          }
        );

      if (response.ok) {

        const data =
          await response.json();

        if (Array.isArray(data)) {
          addExternalMovies(data);
        }

      }

    }

    catch (error) {

      console.error(
        "Movies JSON loading failed:",
        error
      );

    }


    /* =======================================================
       INITIAL JSON MATCH STATUS
       ======================================================= */

    document
      .querySelectorAll(
        '#matchList .match-card[data-external-content="true"]'
      )
      .forEach(
        function (card) {

          updateExternalMatchStatus(
            card,
            new Date()
          );

        }
      );


    sortExternalDateCards(
      ".highlight-card",
      "date"
    );

    sortExternalDateCards(
      ".movie-card",
      "date"
    );


    requestAnimationFrame(
      updateStickyPositions
    );

    console.log(
      "Deeprowss external JSON content loaded."
    );

  }


  /* =========================================================
     UPDATE JSON MATCHES EVERY SECOND
     ========================================================= */

  setInterval(
    function () {

      const now =
        new Date();

      document
        .querySelectorAll(
          '#matchList .match-card[data-external-content="true"]'
        )
        .forEach(
          function (card) {

            updateExternalMatchStatus(
              card,
              now
            );

          }
        );

    },
    1000
  );


  /* =========================================================
     MOBILE MENU / PAGE SAFETY
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


  /* =========================================================
     START EXTERNAL CONTENT
     ========================================================= */

  loadExternalContent();


});
