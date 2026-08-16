document.addEventListener("DOMContentLoaded", function () {


  /* =========================
     MATCH DATA
  ========================= */

  const liveMatches = [

    {
      id: "chelsea-juventus",
      title: "Chelsea vs Juventus",
      status: "LIVE",
      type: "live",
      playerType: "iframe",
      playerUrl: ""
    },

    {
      id: "arsenal-barcelona",
      title: "Arsenal vs Barcelona",
      status: "UPCOMING",
      type: "upcoming",
      playerType: "iframe",
      playerUrl: ""
    },

    {
      id: "real-madrid-bayern",
      title: "Real Madrid vs Bayern",
      status: "UPCOMING",
      type: "upcoming",
      playerType: "iframe",
      playerUrl: ""
    }

  ];


  /* =========================
     HIGHLIGHTS DATA
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
     PAGE ELEMENTS
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


  /* =========================
     RENDER LIVE MATCHES
  ========================= */

  function renderLiveMatches() {

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


      /*
       * Add special class
       * for LIVE / UPCOMING.
       */

      if (match.status === "LIVE") {

        card.classList.add("live-match-card");

      } else {

        card.classList.add("upcoming-match-card");

      }


      /* =========================
         CARD CONTENT
      ========================= */

      const cardTop =
        document.createElement("div");

      cardTop.className =
        "card-top";


      const status =
        document.createElement("span");


      if (match.status === "LIVE") {

        status.className =
          "live-status";

        status.textContent =
          "LIVE";

      } else {

        status.className =
          "upcoming-status";

        status.textContent =
          "UPCOMING";

      }


      cardTop.appendChild(status);


      /* =========================
         MATCH AREA
      ========================= */

      const matchArea =
        document.createElement("div");

      matchArea.className =
        "match-area";


      const teams =
        document.createElement("div");

      teams.className =
        "match-teams";


      const titleParts =
        match.title.split(" vs ");


      const homeTeam =
        document.createElement("span");

      homeTeam.className =
        "team-name";

      homeTeam.textContent =
        titleParts[0] || match.title;


      const versus =
        document.createElement("span");

      versus.className =
        "versus";

      versus.textContent =
        "⚽";


      const awayTeam =
        document.createElement("span");

      awayTeam.className =
        "team-name";

      awayTeam.textContent =
        titleParts[1] || "";


      teams.appendChild(homeTeam);

      teams.appendChild(versus);

      teams.appendChild(awayTeam);


      matchArea.appendChild(teams);


      /* =========================
         WATCH AREA
      ========================= */

      const watch =
        document.createElement("span");

      watch.className =
        "card-watch";


      watch.textContent =
        match.status === "LIVE"
          ? "▶ WATCH"
          : "→";


      /* =========================
         PUT EVERYTHING TOGETHER
      ========================= */

      card.appendChild(cardTop);

      card.appendChild(matchArea);

      card.appendChild(watch);


      /* =========================
         CARD CLICK
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

    highlightsContainer.innerHTML = "";


    if (highlights.length === 0) {

      highlightsContainer.innerHTML =
        '<div class="empty-message">No highlights available</div>';

      return;

    }


    highlights.forEach(function (highlight) {

      const card =
        document.createElement("button");


      card.type = "button";

      card.className =
        "content-card highlight-card";


      /* =========================
         THUMBNAIL AREA
      ========================= */

      const thumbnail =
        document.createElement("div");

      thumbnail.className =
        "highlight-thumbnail";


      const thumbnailIcon =
        document.createElement("span");

      thumbnailIcon.className =
        "highlight-icon";

      thumbnailIcon.textContent =
        "🎬";


      const playIcon =
        document.createElement("span");

      playIcon.className =
        "highlight-play";

      playIcon.textContent =
        "▶";


      thumbnail.appendChild(
        thumbnailIcon
      );

      thumbnail.appendChild(
        playIcon
      );


      /* =========================
         HIGHLIGHT INFORMATION
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
         CARD CLICK
      ========================= */

      card.addEventListener(
        "click",
        function () {

          openHighlightOnScreen(
            highlight
          );

        }
      );


      highlightsContainer.appendChild(
        card
      );

    });

  }


  /* =========================
     PAGE NAVIGATION
  ========================= */

  function showPage(page) {

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

  window.openScreen = function () {

    previousPage = "home";

    showPage(screenPage);

  };


  /* =========================
     OPEN FOOTBALL
  ========================= */

  window.openFootball = function () {

    previousPage = "home";

    showPage(footballPage);

    showLive();

  };


  /* =========================
     GO HOME
  ========================= */

  window.goHome = function () {

    stopCurrentPlayer();

    showPage(homePage);

  };


  /* =========================
     SCREEN BACK BUTTON
  ========================= */

  window.goBackFromScreen = function () {

    stopCurrentPlayer();


    if (previousPage === "football") {

      showPage(footballPage);

    } else {

      showPage(homePage);

    }

  };


  /* =========================
     LIVE TAB
  ========================= */

  window.showLive = function () {

    liveSection.classList.add("active");

    highlightSection.classList.remove("active");


    liveTab.classList.add("active");

    highlightTab.classList.remove("active");

  };


  /* =========================
     HIGHLIGHTS TAB
  ========================= */

  window.showHighlights = function () {

    highlightSection.classList.add("active");

    liveSection.classList.remove("active");


    highlightTab.classList.add("active");

    liveTab.classList.remove("active");

  };


  /* =========================
     OPEN LIVE MATCH
  ========================= */

  function openMatchOnScreen(match) {

    currentItem = match;

    previousPage = "football";


    showPage(screenPage);


    updateScreen(match);

  }


  /* =========================
     OPEN HIGHLIGHT
  ========================= */

  function openHighlightOnScreen(
    highlight
  ) {

    currentItem = highlight;

    previousPage = "football";


    showPage(screenPage);


    updateScreen(highlight);

  }


  /* =========================
     UPDATE SCREEN
  ========================= */

  function updateScreen(item) {

    nowShowing.textContent =
      item.title;


    screenPlayer.innerHTML = "";


    /* =========================
       NO PLAYER URL
    ========================= */

    if (
      !item.playerUrl ||
      item.playerUrl.trim() === ""
    ) {

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


      placeholder.appendChild(icon);

      placeholder.appendChild(title);

      placeholder.appendChild(small);


      screenPlayer.appendChild(
        placeholder
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

      const iframe =
        document.createElement("iframe");


      iframe.src =
        item.playerUrl;


      iframe.allow =
        "autoplay; fullscreen; picture-in-picture";


      iframe.setAttribute(
        "allowfullscreen",
        ""
      );


      iframe.loading =
        "eager";


      iframe.title =
        item.title;


      screenPlayer.appendChild(
        iframe
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

      const video =
        document.createElement("video");


      video.src =
        item.playerUrl;


      video.controls =
        true;


      video.autoplay =
        true;


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


      video.title =
        item.title;


      screenPlayer.appendChild(
        video
      );


      addFullscreenButton();


      return;

    }

  }


  /* =========================
     FULLSCREEN BUTTON
  ========================= */

  function addFullscreenButton() {

    const fullscreenButton =
      document.createElement("button");


    fullscreenButton.type =
      "button";


    fullscreenButton.className =
      "fullscreen-button";


    fullscreenButton.textContent =
      "⛶";


    fullscreenButton.setAttribute(
      "aria-label",
      "Fullscreen"
    );


    fullscreenButton.setAttribute(
      "title",
      "Fullscreen"
    );


    fullscreenButton.addEventListener(
      "click",
      function (event) {

        event.stopPropagation();

        enterFullscreen(
          screenPlayer
        );

      }
    );


    screenPlayer.appendChild(
      fullscreenButton
    );

  }


  /* =========================
     FULLSCREEN
  ========================= */

  function enterFullscreen(
    element
  ) {

    if (
      document.fullscreenElement
    ) {

      if (
        document.exitFullscreen
      ) {

        document.exitFullscreen();

      }

      return;

    }


    if (
      element.requestFullscreen
    ) {

      element.requestFullscreen()
        .catch(function (error) {

          console.log(
            "Fullscreen request failed:",
            error
          );

        });

      return;

    }


    /*
     * WebKit fallback
     * for supported browsers.
     */

    if (
      element.webkitRequestFullscreen
    ) {

      element.webkitRequestFullscreen();

    }

  }


  /* =========================
     FULLSCREEN CHANGE
  ========================= */

  document.addEventListener(
    "fullscreenchange",
    function () {

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
  );


  /* =========================
     STOP PLAYER
  ========================= */

  function stopCurrentPlayer() {

    if (!screenPlayer) {
      return;
    }


    /*
     * Exit fullscreen first.
     */

    if (
      document.fullscreenElement &&
      document.exitFullscreen
    ) {

      document.exitFullscreen()
        .catch(function () {});

    }


    screenPlayer.innerHTML = "";


    nowShowing.textContent =
      "Select a match";


    currentItem = null;

  }


  /* =========================
     INITIAL LOAD
  ========================= */

  renderLiveMatches();

  renderHighlights();

});
