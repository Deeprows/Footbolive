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

      homeLogo: "icons/chelsea.png",
      awayLogo: "icons/juventus.png",

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

      homeLogo: "icons/arsenal.png",
      awayLogo: "icons/barcelona.png",

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

      homeLogo: "icons/real-madrid.png",
      awayLogo: "icons/bayern.png",

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

      homeTeam: "Chelsea",
      awayTeam: "Juventus",

      thumbnail:
        "icons/chelsea-juventus.jpg",

      playerType: "iframe",
      playerUrl: ""
    },


    {
      id: "arsenal-barcelona-highlight",

      title: "Arsenal vs Barcelona",

      status: "Highlight",

      homeTeam: "Arsenal",
      awayTeam: "Barcelona",

      thumbnail:
        "icons/arsenal-barcelona.jpg",

      playerType: "iframe",
      playerUrl: ""
    },


    {
      id: "real-madrid-bayern-highlight",

      title: "Real Madrid vs Bayern",

      status: "Highlight",

      homeTeam: "Real Madrid",
      awayTeam: "Bayern",

      thumbnail:
        "icons/real-madrid-bayern.jpg",

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


  /* =========================
     SAFE IMAGE LOADER
  ========================= */

  function createTeamLogo(
    src,
    teamName
  ) {

    const wrapper =
      document.createElement("div");

    wrapper.className =
      "team-logo";


    if (!src) {

      wrapper.textContent =
        "⚽";

      return wrapper;

    }


    const image =
      document.createElement("img");


    image.src = src;

    image.alt =
      teamName;


    image.loading =
      "lazy";


    image.onerror =
      function () {

        wrapper.innerHTML =
          "⚽";

      };


    wrapper.appendChild(image);


    return wrapper;

  }


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


    liveMatches.forEach(
      function (match) {

        const card =
          document.createElement("button");


        card.type =
          "button";


        card.className =
          "content-card";


        if (
          match.status === "LIVE"
        ) {

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


        if (
          match.status === "LIVE"
        ) {

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


        cardTop.appendChild(
          status
        );


        /* =========================
           MATCH TIME
        ========================= */

        const matchTime =
          document.createElement("span");


        matchTime.className =
          "match-time";


        matchTime.textContent =
          match.time || "";


        cardTop.appendChild(
          matchTime
        );


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
          createTeamLogo(
            match.homeLogo,
            match.homeTeam
          );


        const homeName =
          document.createElement("span");


        homeName.className =
          "team-name";


        homeName.textContent =
          match.homeTeam;


        home.appendChild(
          homeLogo
        );

        home.appendChild(
          homeName
        );


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


        center.appendChild(
          versus
        );


        const away =
          document.createElement("div");


        away.className =
          "team";


        const awayLogo =
          createTeamLogo(
            match.awayLogo,
            match.awayTeam
          );


        const awayName =
          document.createElement("span");


        awayName.className =
          "team-name";


        awayName.textContent =
          match.awayTeam;


        away.appendChild(
          awayLogo
        );

        away.appendChild(
          awayName
        );


        matchArea.appendChild(
          home
        );

        matchArea.appendChild(
          center
        );

        matchArea.appendChild(
          away
        );


        /* =========================
           WATCH BUTTON
        ========================= */

        const watch =
          document.createElement("span");


        watch.className =
          "card-watch";


        if (
          match.status === "LIVE"
        ) {

          watch.textContent =
            "▶ WATCH";

        } else {

          watch.textContent =
            "VIEW";

        }


        /* =========================
           BUILD CARD
        ========================= */

        card.appendChild(
          cardTop
        );

        card.appendChild(
          matchArea
        );

        card.appendChild(
          watch
        );


        /* =========================
           CLICK
        ========================= */

        card.addEventListener(
          "click",
          function () {

            openMatchOnScreen(
              match
            );

          }
        );


        liveMatchesContainer.appendChild(
          card
        );

      }
    );

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


    highlights.forEach(
      function (highlight) {

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


        if (
          highlight.thumbnail
        ) {

          const image =
            document.createElement("img");


          image.src =
            highlight.thumbnail;


          image.alt =
            highlight.title;


          image.loading =
            "lazy";


          image.onerror =
            function () {

              image.remove();

            };


          thumbnail.appendChild(
            image
          );

        }


        /* =========================
           FALLBACK ICON
        ========================= */

        const highlightIcon =
          document.createElement("span");


        highlightIcon.className =
          "highlight-icon";


        highlightIcon.textContent =
          "🎬";


        thumbnail.appendChild(
          highlightIcon
        );


        /* =========================
           PLAY BUTTON
        ========================= */

        const play =
          document.createElement("span");


        play.className =
          "highlight-play";


        play.textContent =
          "▶";


        thumbnail.appendChild(
          play
        );


        /* =========================
           INFORMATION
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


        info.appendChild(
          title
        );

        info.appendChild(
          status
        );


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

        card.appendChild(
          thumbnail
        );

        card.appendChild(
          info
        );

        card.appendChild(
          arrow
        );


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


        highlightsContainer.appendChild(
          card
        );

      }
    );

  }


  /* =========================
     PAGE NAVIGATION
  ========================= */

  function showPage(page) {

    homePage.classList.remove(
      "active"
    );

    footballPage.classList.remove(
      "active"
    );

    screenPage.classList.remove(
      "active"
    );


    page.classList.add(
      "active"
    );


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

      previousPage =
        "home";


      showPage(
        screenPage
      );

    };


  /* =========================
     OPEN FOOTBALL
  ========================= */

  window.openFootball =
    function () {

      previousPage =
        "home";


      showPage(
        footballPage
      );


      showLive();

    };


  /* =========================
     HOME
  ========================= */

  window.goHome =
    function () {

      stopCurrentPlayer();


      showPage(
        homePage
      );

    };


  /* =========================
     SCREEN BACK
  ========================= */

  window.goBackFromScreen =
    function () {

      stopCurrentPlayer();


      if (
        previousPage ===
        "football"
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

  function openMatchOnScreen(
    match
  ) {

    currentItem =
      match;


    previousPage =
      "football";


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

    currentItem =
      highlight;


    previousPage =
      "football";


    showPage(
      screenPage
    );


    updateScreen(
      highlight
    );

  }


  /* =========================
     UPDATE SCREEN
  ========================= */

  function updateScreen(
    item
  ) {

    nowShowing.textContent =
      item.title;


    screenPlayer.innerHTML =
      "";


    /* =========================
       NO PLAYER
    ========================= */

    if (
      !item.playerUrl ||
      item.playerUrl.trim() === ""
    ) {

      const placeholder =
        document.createElement(
          "div"
        );


      placeholder.className =
        "screen-placeholder";


      const icon =
        document.createElement(
          "div"
        );


      icon.className =
        "screen-icon";


      icon.textContent =
        "📺";


      const title =
        document.createElement(
          "p"
        );


      title.textContent =
        item.title;


      const small =
        document.createElement(
          "small"
        );


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


      addFullscreenButton();


      return;

    }


    /* =========================
       IFRAME
    ========================= */

    if (
      item.playerType ===
      "iframe"
    ) {

      const iframe =
        document.createElement(
          "iframe"
        );


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
       VIDEO
    ========================= */

    if (
      item.playerType ===
      "video"
    ) {

      const video =
        document.createElement(
          "video"
        );


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
      document.createElement(
        "button"
      );


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


    if (
      document.fullscreenElement &&
      document.exitFullscreen
    ) {

      document.exitFullscreen()
        .catch(
          function () {}
        );

    }


    screenPlayer.innerHTML =
      "";


    nowShowing.textContent =
      "Select a match";


    currentItem =
      null;

  }


  /* =========================
     INITIAL LOAD
  ========================= */

  renderLiveMatches();

  renderHighlights();

});
