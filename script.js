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

      /*
       * Add your video/stream URL here.
       * For an iframe:
       * playerType: "iframe"
       *
       * For a video file:
       * playerType: "video"
       */

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
    }
  ];


  /* =========================
     HIGHLIGHT DATA
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
     CURRENT SCREEN ITEM
  ========================= */

  let currentItem = null;

  let previousPage = "home";


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

      card.className =
        "content-card";


      const statusClass =
        match.status === "LIVE"
          ? "live-status"
          : "upcoming-status";


      card.innerHTML = `

        <div class="card-info">

          <div class="card-title">
            ${match.title}
          </div>

          <div class="${statusClass}">
            ${match.status}
          </div>

        </div>

        <div class="card-arrow">
          →
        </div>

      `;


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

      card.className =
        "content-card";


      card.innerHTML = `

        <div class="card-info">

          <div class="card-title">
            ${highlight.title}
          </div>

          <div class="card-status">
            ${highlight.status}
          </div>

        </div>

        <div class="card-arrow">
          →
        </div>

      `;


      card.addEventListener(
        "click",
        function () {

          openHighlightOnScreen(highlight);

        }
      );


      highlightsContainer.appendChild(card);

    });

  }


  /* =========================
     SHOW PAGE
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

    showPage(homePage);

  };


  /* =========================
     SCREEN BACK BUTTON
  ========================= */

  window.goBackFromScreen = function () {

    if (previousPage === "football") {

      showPage(footballPage);

    } else {

      showPage(homePage);

    }

  };


  /* =========================
     SHOW LIVE
  ========================= */

  window.showLive = function () {

    liveSection.classList.add("active");

    highlightSection.classList.remove("active");

    liveTab.classList.add("active");

    highlightTab.classList.remove("active");

  };


  /* =========================
     SHOW HIGHLIGHTS
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

  function openHighlightOnScreen(highlight) {

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


    /*
     * No player URL yet
     */

    if (
      !item.playerUrl ||
      item.playerUrl.trim() === ""
    ) {

      screenPlayer.innerHTML = `

        <div class="screen-placeholder">

          <div class="screen-icon">
            📺
          </div>

          <p>
            ${item.title}
          </p>

        </div>

      `;

      return;

    }


    /* =========================
       IFRAME PLAYER
    ========================= */

    if (item.playerType === "iframe") {

      const iframe =
        document.createElement("iframe");

      iframe.src =
        item.playerUrl;

      iframe.allow =
        "autoplay; fullscreen; picture-in-picture";

      iframe.allowFullscreen = true;

      iframe.loading = "lazy";

      screenPlayer.appendChild(iframe);

      return;

    }


    /* =========================
       VIDEO PLAYER
    ========================= */

    if (item.playerType === "video") {

      const video =
        document.createElement("video");

      video.src =
        item.playerUrl;

      video.controls = true;

      video.autoplay = true;

      video.playsInline = true;

      screenPlayer.appendChild(video);

      return;

    }

  }


  /* =========================
     INITIAL LOAD
  ========================= */

  renderLiveMatches();

  renderHighlights();

});
