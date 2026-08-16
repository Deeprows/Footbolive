document.addEventListener("DOMContentLoaded", function () {

  /* =========================
     MATCH DATA
  ========================= */

  const liveMatches = [

    {
      id: "chelsea-juventus",
      homeTeam: "Chelsea",
      awayTeam: "Juventus",
      title: "Chelsea vs Juventus",

      status: "LIVE",
      type: "live",
      time: "LIVE",

      playerType: "iframe",
      playerUrl: ""
    },

    {
      id: "arsenal-barcelona",
      homeTeam: "Arsenal",
      awayTeam: "Barcelona",
      title: "Arsenal vs Barcelona",

      status: "UPCOMING",
      type: "upcoming",
      time: "17:00",

      playerType: "iframe",
      playerUrl: ""
    },

    {
      id: "real-madrid-bayern",
      homeTeam: "Real Madrid",
      awayTeam: "Bayern",
      title: "Real Madrid vs Bayern",

      status: "UPCOMING",
      type: "upcoming",
      time: "20:00",

      playerType: "iframe",
      playerUrl: ""
    }

  ];


  /* =========================
     HIGHLIGHTS
  ========================= */

  const highlights = [

    {
      id: "chelsea-juventus-highlight",
      title: "Chelsea vs Juventus",
      status: "Highlight",

      playerType: "iframe",
      playerUrl: ""
    },

    {
      id: "arsenal-barcelona-highlight",
      title: "Arsenal vs Barcelona",
      status: "Highlight",

      playerType: "iframe",
      playerUrl: ""
    },

    {
      id: "real-madrid-bayern-highlight",
      title: "Real Madrid vs Bayern",
      status: "Highlight",

      playerType: "iframe",
      playerUrl: ""
    }

  ];


  /* =========================
     ELEMENTS
  ========================= */

  const homePage =
    document.getElementById("homePage");

  const footballPage =
    document.getElementById("footballPage");

  const screenPage =
    document.getElementById("screenPage");

  const liveSection =
    document.getElementById("liveSection");

  const highlightSection =
    document.getElementById("highlightSection");

  const liveTab =
    document.getElementById("liveTab");

  const highlightTab =
    document.getElementById("highlightTab");

  const liveMatchesContainer =
    document.getElementById("liveMatches");

  const highlightsContainer =
    document.getElementById("highlights");

  const nowShowing =
    document.getElementById("nowShowing");

  const screenPlayer =
    document.getElementById("screenPlayer");


  /* =========================
     APP STATE
  ========================= */

  let previousPage = "home";

  let currentItem = null;

  let currentPlayer = null;


  /* =========================
     RENDER LIVE MATCHES
  ========================= */

  function renderLiveMatches() {

    if (!liveMatchesContainer) {
      return;
    }


    liveMatchesContainer.innerHTML = "";


    if (liveMatches.length === 0) {

      liveMatchesContainer.innerHTML =
        '<div class="empty-message">No matches available</div>';

      return;

    }


    liveMatches.forEach(function (match) {

      const card =
        document.createElement("button");


      card.type = "button";

      card.className =
        "content-card";


      if (match.status === "LIVE") {

        card.classList.add(
          "live-match-card"
        );

      } else {

        card.classList.add(
          "upcoming-match-card"
        );

      }


      /* =========================
         CARD TOP
      ========================= */

      const cardTop =
        document.createElement("div");


      cardTop.className =
        "card-top";


      const status =
        document.createElement("span");


      status.className =
        match.status === "LIVE"
          ? "live-status"
          : "upcoming-status";


      status.textContent =
        match.status;


      const matchTime =
        document.createElement("span");


      matchTime.className =
        "match-time";


      matchTime.textContent =
        match.time || "";


      cardTop.appendChild(status);

      cardTop.appendChild(matchTime);


      /* =========================
         MATCH AREA
      ========================= */

      const matchArea =
        document.createElement("div");


      matchArea.className =
        "match-area";


      const home =
        document.createElement("div");


      home.className =
        "team";


      const homeLogo =
        document.createElement("div");


      homeLogo.className =
        "team-logo";


      homeLogo.textContent =
        "⚽";


      const homeName =
        document.createElement("span");


      homeName.className =
        "team-name";


      homeName.textContent =
        match.homeTeam;


      home.appendChild(homeLogo);

      home.appendChild(homeName);


      /* =========================
         CENTER
      ========================= */

      const center =
        document.createElement("div");


      center.className =
        "match-center";


      const versus =
        document.createElement("span");


      versus.className =
        "versus";


      versus.textContent =
        "VS";


      center.appendChild(versus);


      /* =========================
         AWAY TEAM
      ========================= */

      const away =
        document.createElement("div");


      away.className =
        "team";


      const awayLogo =
        document.createElement("div");


      awayLogo.className =
        "team-logo";


      awayLogo.textContent =
        "⚽";


      const awayName =
        document.createElement("span");


      awayName.className =
        "team-name";


      awayName.textContent =
        match.awayTeam;


      away.appendChild(awayLogo);

      away.appendChild(awayName);


      matchArea.appendChild(home);

      matchArea.appendChild(center);

      matchArea.appendChild(away);


      /* =========================
         WATCH BUTTON
      ========================= */

      const watch =
        document.createElement("span");


      watch.className =
        "card-watch";


      watch.textContent =
        match.status === "LIVE"
          ? "▶ WATCH"
          : "VIEW";


      /* =========================
         BUILD CARD
      ========================= */

      card.appendChild(cardTop);

      card.appendChild(matchArea);

      card.appendChild(watch);


      /* =========================
         CLICK
      ========================= */

      card.addEventListener(
        "click",
        function () {

          openMatchOnScreen(match);

        }
      );


      liveMatchesContainer.appendChild(card);

    });

  }


  /* =========================
     RENDER HIGHLIGHTS
  ========================= */

  function renderHighlights() {

    if (!highlightsContainer) {
      return;
    }


    highlightsContainer.innerHTML = "";


    if (highlights.length === 0) {

      highlightsContainer.innerHTML =
        '<div class="empty-message">No highlights available</div>';

      return;

    }


    highlights.forEach(function (highlight) {

      const card =
        document.createElement("button");


      card.type =
        "button";


      card.className =
        "content-card highlight-card";


      /* =========================
         THUMBNAIL
      ========================= */

      const thumbnail =
        document.createElement("div");


      thumbnail.className =
        "highlight-thumbnail";


      const icon =
        document.createElement("span");


      icon.className =
        "highlight-icon";


      icon.textContent =
        "🎬";


      thumbnail.appendChild(icon);


      /* =========================
         PLAY
      ========================= */

      const play =
        document.createElement("span");


      play.className =
        "highlight-play";


      play.textContent =
        "▶";


      thumbnail.appendChild(play);


      /* =========================
         INFO
      ========================= */

      const info =
        document.createElement("div");


      info.className =
        "highlight-info";


      const title =
        document.createElement("div");


      title.className =
        "card-title";


      title.textContent =
        highlight.title;


      const status =
        document.createElement("div");


      status.className =
        "card-status";


      status.textContent =
        "HIGHLIGHT";


      info.appendChild(title);

      info.appendChild(status);


      /* =========================
         ARROW
      ========================= */

      const arrow =
        document.createElement("div");


      arrow.className =
        "card-arrow";


      arrow.textContent =
        "→";


      /* =========================
         BUILD CARD
      ========================= */

      card.appendChild(thumbnail);

      card.appendChild(info);

      card.appendChild(arrow);


      /* =========================
         CLICK
      ========================= */

      card.addEventListener(
        "click",
        function () {

          openHighlightOnScreen(
            highlight
          );

        }
      );


      highlightsContainer.appendChild(card);

    });

  }


  /* =========================
     PAGE NAVIGATION
  ========================= */

  function showPage(page) {

    if (!page) {
      return;
    }


    homePage.classList.remove("active");

    footballPage.classList.remove("active");

    screenPage.classList.remove("active");


    page.classList.add("active");


    window.scrollTo({
      top: 0,
      behavior: "instant"
    });

  }


  /* =========================
     OPEN SCREEN
  ========================= */

  window.openScreen =
    function () {

      previousPage = "home";

      showPage(screenPage);

    };


  /* =========================
     OPEN FOOTBALL
  ========================= */

  window.openFootball =
    function () {

      previousPage = "home";

      showPage(footballPage);

      showLive();

    };


  /* =========================
     HOME
  ========================= */

  window.goHome =
    function () {

      stopCurrentPlayer();

      showPage(homePage);

    };


  /* =========================
     BACK FROM SCREEN
  ========================= */

  window.goBackFromScreen =
    function () {

      stopCurrentPlayer();


      if (
        previousPage === "football"
      ) {

        showPage(
          footballPage
        );

      } else {

        showPage(
          homePage
        );

      }

    };


  /* =========================
     LIVE TAB
  ========================= */

  window.showLive =
    function () {

      if (
        !liveSection ||
        !highlightSection
      ) {
        return;
      }


      liveSection.classList.add(
        "active"
      );


      highlightSection.classList.remove(
        "active"
      );


      liveTab.classList.add(
        "active"
      );


      highlightTab.classList.remove(
        "active"
      );

    };


  /* =========================
     HIGHLIGHTS TAB
  ========================= */

  window.showHighlights =
    function () {

      highlightSection.classList.add(
        "active"
      );


      liveSection.classList.remove(
        "active"
      );


      highlightTab.classList.add(
        "active"
      );


      liveTab.classList.remove(
        "active"
      );

    };


  /* =========================
     OPEN MATCH
  ========================= */

  function openMatchOnScreen(match) {

    previousPage =
      "football";


    currentItem =
      match;


    showPage(
      screenPage
    );


    updateScreen(
      match
    );

  }


  /* =========================
     OPEN HIGHLIGHT
  ========================= */

  function openHighlightOnScreen(
    highlight
  ) {

    previousPage =
      "football";


    currentItem =
      highlight;


    showPage(
      screenPage
    );


    updateScreen(
      highlight
    );

  }


  /* =========================
     CLEAR PLAYER
  ========================= */

  function clearPlayer() {

    if (!screenPlayer) {
      return;
    }


    /* Stop HTML5 video */

    if (
      currentPlayer &&
      currentPlayer.tagName === "VIDEO"
    ) {

      try {

        currentPlayer.pause();

        currentPlayer.removeAttribute(
          "src"
        );

        currentPlayer.load();

      } catch (error) {

        console.log(
          "Video cleanup error:",
          error
        );

      }

    }


    currentPlayer =
      null;


    screenPlayer.innerHTML =
      "";

  }


  /* =========================
     UPDATE SCREEN
  ========================= */

  function updateScreen(item) {

    if (
      !item ||
      !screenPlayer
    ) {
      return;
    }


    clearPlayer();


    /* =========================
       NOW SHOWING
    ========================= */

    if (nowShowing) {

      nowShowing.textContent =
        item.title;

    }


    /* =========================
       NO PLAYER URL
    ========================= */

    if (
      !item.playerUrl ||
      item.playerUrl.trim() === ""
    ) {

      createPlaceholder(
        item
      );


      addFullscreenButton();


      return;

    }


    /* =========================
       IFRAME PLAYER
    ========================= */

    if (
      item.playerType === "iframe"
    ) {

      createIframePlayer(
        item
      );


      addFullscreenButton();


      return;

    }


    /* =========================
       VIDEO PLAYER
    ========================= */

    if (
      item.playerType === "video"
    ) {

      createVideoPlayer(
        item
      );


      addFullscreenButton();


      return;

    }


    /* =========================
       UNKNOWN PLAYER
    ========================= */

    createPlaceholder(
      item
    );


    addFullscreenButton();

  }


  /* =========================
     CREATE PLACEHOLDER
  ========================= */

  function createPlaceholder(item) {

    const placeholder =
      document.createElement("div");


    placeholder.className =
      "screen-placeholder";


    const icon =
      document.createElement("div");


    icon.className =
      "screen-icon";


    icon.textContent =
      "📺";


    const title =
      document.createElement("p");


    title.textContent =
      item.title;


    const small =
      document.createElement("small");


    small.textContent =
      "Player ready";


    placeholder.appendChild(
      icon
    );


    placeholder.appendChild(
      title
    );


    placeholder.appendChild(
      small
    );


    screenPlayer.appendChild(
      placeholder
    );

  }


  /* =========================
     CREATE IFRAME
  ========================= */

  function createIframePlayer(item) {

    const iframe =
      document.createElement("iframe");


    iframe.src =
      item.playerUrl;


    iframe.title =
      item.title;


    iframe.allow =
      "autoplay; fullscreen; picture-in-picture";


    iframe.setAttribute(
      "allowfullscreen",
      ""
    );


    iframe.setAttribute(
      "webkitallowfullscreen",
      ""
    );


    iframe.setAttribute(
      "mozallowfullscreen",
      ""
    );


    iframe.loading =
      "eager";


    iframe.referrerPolicy =
      "strict-origin-when-cross-origin";


    screenPlayer.appendChild(
      iframe
    );


    currentPlayer =
      iframe;

  }


  /* =========================
     CREATE VIDEO
  ========================= */

  function createVideoPlayer(item) {

    const video =
      document.createElement("video");


    video.src =
      item.playerUrl;


    video.controls =
      true;


    video.autoplay =
      true;


    video.muted =
      false;


    video.playsInline =
      true;


    video.setAttribute(
      "playsinline",
      ""
    );


    video.setAttribute(
      "webkit-playsinline",
      ""
    );


    video.preload =
      "auto";


    video.controlsList =
      "nodownload";


    screenPlayer.appendChild(
      video
    );


    currentPlayer =
      video;


    /* =========================
       AUTOPLAY FALLBACK
    ========================= */

    const playPromise =
      video.play();


    if (
      playPromise &&
      typeof playPromise.catch ===
      "function"
    ) {

      playPromise.catch(
        function () {

          /*
             Some browsers block
             autoplay with sound.

             The native video controls
             remain available.
          */

          console.log(
            "Autoplay was blocked."
          );

        }
      );

    }

  }


  /* =========================
     FULLSCREEN BUTTON
  ========================= */

  function addFullscreenButton() {

    if (!screenPlayer) {
      return;
    }


    const button =
      document.createElement("button");


    button.type =
      "button";


    button.className =
      "fullscreen-button";


    button.textContent =
      "⛶";


    button.setAttribute(
      "aria-label",
      "Fullscreen"
    );


    button.setAttribute(
      "title",
      "Fullscreen"
    );


    button.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        event.stopPropagation();


        toggleFullscreen();

      }
    );


    screenPlayer.appendChild(
      button
    );


    updateFullscreenButton();

  }


  /* =========================
     TOGGLE FULLSCREEN
  ========================= */

  function toggleFullscreen() {

    if (
      document.fullscreenElement
    ) {

      exitFullscreen();

      return;

    }


    enterFullscreen();

  }


  /* =========================
     ENTER FULLSCREEN
  ========================= */

  function enterFullscreen() {

    if (!screenPlayer) {
      return;
    }


    if (
      screenPlayer.requestFullscreen
    ) {

      screenPlayer
        .requestFullscreen()
        .catch(
          function (error) {

            console.log(
              "Fullscreen request failed:",
              error
            );

          }
        );

      return;

    }


    if (
      screenPlayer.webkitRequestFullscreen
    ) {

      screenPlayer.webkitRequestFullscreen();

      return;

    }


    if (
      screenPlayer.msRequestFullscreen
    ) {

      screenPlayer.msRequestFullscreen();

    }

  }


  /* =========================
     EXIT FULLSCREEN
  ========================= */

  function exitFullscreen() {

    if (
      document.exitFullscreen
    ) {

      document.exitFullscreen()
        .catch(
          function () {}
        );

      return;

    }


    if (
      document.webkitExitFullscreen
    ) {

      document.webkitExitFullscreen();

    }

  }


  /* =========================
     UPDATE FULLSCREEN BUTTON
  ========================= */

  function updateFullscreenButton() {

    if (!screenPlayer) {
      return;
    }


    const button =
      screenPlayer.querySelector(
        ".fullscreen-button"
      );


    if (!button) {
      return;
    }


    if (
      document.fullscreenElement
    ) {

      button.textContent =
        "✕";


      button.setAttribute(
        "aria-label",
        "Exit fullscreen"
      );


      button.setAttribute(
        "title",
        "Exit fullscreen"
      );

    } else {

      button.textContent =
        "⛶";


      button.setAttribute(
        "aria-label",
        "Fullscreen"
      );


      button.setAttribute(
        "title",
        "Fullscreen"
      );

    }

  }


  /* =========================
     FULLSCREEN EVENTS
  ========================= */

  document.addEventListener(
    "fullscreenchange",
    function () {

      updateFullscreenButton();

    }
  );


  document.addEventListener(
    "webkitfullscreenchange",
    function () {

      updateFullscreenButton();

    }
  );


  /* =========================
     STOP PLAYER
  ========================= */

  function stopCurrentPlayer() {

    if (!screenPlayer) {
      return;
    }


    /* Exit fullscreen */

    if (
      document.fullscreenElement
    ) {

      exitFullscreen();

    }


    clearPlayer();


    if (nowShowing) {

      nowShowing.textContent =
        "Select a match";

    }


    currentItem =
      null;

  }


  /* =========================
     KEYBOARD ESCAPE
  ========================= */

  document.addEventListener(
    "keydown",
    function (event) {

      if (
        event.key === "Escape" &&
        document.fullscreenElement
      ) {

        exitFullscreen();

      }

    }
  );


  /* =========================
     INITIAL LOAD
  ========================= */

  renderLiveMatches();

  renderHighlights();

});
