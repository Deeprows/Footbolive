document.addEventListener("DOMContentLoaded", function () {

  "use strict";


  console.log("Deeprowss app loaded");


  /* =========================================================
     ELEMENTS
     ========================================================= */

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
     APP BUTTONS
     ========================================================= */

  const footballButton =
    document.getElementById("footballButton");

  const highlightsButton =
    document.getElementById("highlightsButton");

  const tvButton =
    document.getElementById("tvButton");


  /* =========================================================
     CONTENT SECTIONS
     ========================================================= */

  const footballContent =
    document.getElementById("footballContent");

  const highlightsContent =
    document.getElementById("highlightsContent");

  const tvContent =
    document.getElementById("tvContent");


  /* =========================================================
     NAVIGATION
     ========================================================= */

  const navFootball =
    document.getElementById("navFootball");

  const navHighlights =
    document.getElementById("navHighlights");

  const navTV =
    document.getElementById("navTV");


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
     UPDATE ACTIVE APP BUTTON
     ========================================================= */

  function setActiveButton(button) {

    [
      footballButton,
      highlightsButton,
      tvButton
    ].forEach(function (item) {

      if (item) {

        item.classList.remove(
          "active"
        );

      }

    });


    if (button) {

      button.classList.add(
        "active"
      );

    }

  }


  /* =========================================================
     UPDATE ACTIVE NAV
     ========================================================= */

  function setActiveNav(link) {

    [
      navFootball,
      navHighlights,
      navTV
    ].forEach(function (item) {

      if (item) {

        item.classList.remove(
          "active"
        );

      }

    });


    if (link) {

      link.classList.add(
        "active"
      );

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

  }


  /* =========================================================
     OPEN FOOTBALL LIVE
     ========================================================= */

  function openFootball() {

    showContent("football");

    setActiveButton(
      footballButton
    );

    setActiveNav(
      navFootball
    );

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
     OPEN TV CHANNELS
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
     FOOTBALL BUTTON
     ========================================================= */

  if (footballButton) {

    footballButton.addEventListener(
      "click",
      function () {

        openFootball();

      }
    );

  }


  /* =========================================================
     HIGHLIGHTS BUTTON
     ========================================================= */

  if (highlightsButton) {

    highlightsButton.addEventListener(
      "click",
      function () {

        openHighlights();

      }
    );

  }


  /* =========================================================
     TV BUTTON
     ========================================================= */

  if (tvButton) {

    tvButton.addEventListener(
      "click",
      function () {

        openTV();

      }
    );

  }


  /* =========================================================
     NAV FOOTBALL
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


  /* =========================================================
     NAV HIGHLIGHTS
     ========================================================= */

  if (navHighlights) {

    navHighlights.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        openHighlights();

      }
    );

  }


  /* =========================================================
     NAV TV
     ========================================================= */

  if (navTV) {

    navTV.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        openTV();

      }
    );

  }


  /* =========================================================
     LOAD SCREEN
     ========================================================= */

  function loadScreen(
    url,
    name,
    type
  ) {

    if (!screenFrame) {
      return;
    }


    if (!url) {
      return;
    }


    /*
     * Update screen information.
     */

    if (nowShowing) {

      nowShowing.textContent =
        name;

    }


    if (screenStatus) {

      screenStatus.textContent =
        "LIVE";

    }


    /*
     * Hide placeholder.
     */

    if (screenPlaceholder) {

      screenPlaceholder.classList.add(
        "hidden"
      );

    }


    /*
     * Dim screen while changing
     * content.
     */

    screenFrame.style.opacity =
      "0.25";


    /*
     * Stop previous content.
     */

    screenFrame.src =
      "about:blank";


    /*
     * Load new content.
     */

    setTimeout(
      function () {

        screenFrame.src =
          url;

        screenFrame.style.opacity =
          "1";

      },
      150
    );


    /*
     * Update status based on
     * content type.
     */

    if (screenStatus) {

      if (type === "tv") {

        screenStatus.textContent =
          "LIVE TV";

      } else if (type === "highlight") {

        screenStatus.textContent =
          "HIGHLIGHT";

      } else {

        screenStatus.textContent =
          "LIVE";

      }

    }


    /*
     * Bring screen into view.
     */

    if (screenPlayer) {

      screenPlayer.scrollIntoView({
        behavior: "smooth",
        block: "start"
      });

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

          const name =
            this.dataset.name;


          loadScreen(
            url,
            name,
            "match"
          );

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
            this.dataset.name;


          loadScreen(
            url,
            name,
            "highlight"
          );

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
            this.dataset.name;


          /*
           * Remove active state
           * from all channels.
           */

          tvChannels.forEach(
            function (item) {

              item.classList.remove(
                "active"
              );

            }
          );


          /*
           * Mark selected channel.
           */

          this.classList.add(
            "active"
          );


          /*
           * Load channel into
           * the main Screen.
           */

          loadScreen(
            url,
            name,
            "tv"
          );


          /*
           * Keep TV section visible.
           */

          openTV();

        }
      );

    }
  );


  /* =========================================================
     FULLSCREEN
     ========================================================= */

  if (fullscreenButton) {

    fullscreenButton.addEventListener(
      "click",
      function () {

        if (
          document.fullscreenElement
        ) {

          document.exitFullscreen();

          return;

        }


        if (
          screenPlayer &&
          screenPlayer.requestFullscreen
        ) {

          screenPlayer.requestFullscreen();

        }

      }
    );

  }


  /* =========================================================
     MOBILE MENU
     ========================================================= */

  const menuToggle =
    document.getElementById(
      "menuToggle"
    );

  const mainNav =
    document.getElementById(
      "mainNav"
    );


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
     INITIAL STATE
     ========================================================= */

  openFootball();


});
