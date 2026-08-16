document.addEventListener("DOMContentLoaded", function () {

  "use strict";


  /* =========================================================
     DEEPROWSS FOOTBALL APP
     ========================================================= */

  console.log("Deeprowss Football App loaded");


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

  const screenPlayer =
    document.getElementById("screenPlayer");

  const fullscreenButton =
    document.getElementById("fullscreenButton");

  const footballButton =
    document.getElementById("footballButton");

  const highlightsButton =
    document.getElementById("highlightsButton");

  const footballContent =
    document.getElementById("footballContent");

  const highlightsContent =
    document.getElementById("highlightsContent");

  const matchList =
    document.getElementById("matchList");

  const highlightList =
    document.getElementById("highlightList");

  const matchCount =
    document.getElementById("matchCount");

  const highlightCount =
    document.getElementById("highlightCount");

  const footballEmpty =
    document.getElementById("footballEmpty");

  const highlightEmpty =
    document.getElementById("highlightEmpty");

  const navFootball =
    document.getElementById("navFootball");

  const navHighlights =
    document.getElementById("navHighlights");

  const menuToggle =
    document.getElementById("menuToggle");

  const mainNav =
    document.getElementById("mainNav");


  /* =========================================================
     CURRENT STATE
     ========================================================= */

  let currentMode = "football";

  let currentItem = null;


  /* =========================================================
     MATCH LINKS
     ========================================================= */

  const matchLinks = {

    "Chelsea vs Juventus":
      "https://filemoon.org/en/E1x3AqDe3yk6/embed"

  };


  /* =========================================================
     HIGHLIGHT LINKS
     ========================================================= */

  const highlightLinks = {

    "Chelsea vs Juventus Highlights":
      "https://filemoon.org/en/LJlGnPwv315w/embed"

  };


  /* =========================================================
     APPLY MATCH LINKS
     ========================================================= */

  function applyMatchLinks() {

    if (!matchList) {
      return;
    }


    matchList
      .querySelectorAll(".match-card")
      .forEach(function (button) {

        const name =
          button.dataset.name || "";

        if (
          matchLinks[name]
        ) {

          button.dataset.url =
            matchLinks[name];

        }

      });

  }


  /* =========================================================
     APPLY HIGHLIGHT LINKS
     ========================================================= */

  function applyHighlightLinks() {

    if (!highlightList) {
      return;
    }


    highlightList
      .querySelectorAll(".highlight-card")
      .forEach(function (button) {

        const name =
          button.dataset.name || "";

        if (
          highlightLinks[name]
        ) {

          button.dataset.url =
            highlightLinks[name];

        }

      });

  }


  /* =========================================================
     RESET SCREEN
     ========================================================= */

  function resetScreen() {

    if (!screenFrame) {
      return;
    }


    screenFrame.src =
      "about:blank";


    screenFrame.style.opacity =
      "1";


    if (screenPlaceholder) {

      screenPlaceholder.style.display =
        "flex";

    }


    if (screenStatus) {

      screenStatus.textContent =
        "READY";

    }


    if (nowShowing) {

      nowShowing.textContent =
        "Select a match";

    }


    currentItem =
      null;


    document
      .querySelectorAll(
        ".match-card, .highlight-card"
      )
      .forEach(function (item) {

        item.classList.remove(
          "active"
        );

      });

  }


  /* =========================================================
     LOAD CONTENT INTO SCREEN
     ========================================================= */

  function loadItem(button, type) {

    if (!button) {
      return;
    }


    const name =
      button.dataset.name ||
      "Selected content";


    const url =
      button.dataset.url ||
      "";


    currentItem = {

      name: name,

      url: url,

      type: type

    };


    /*
     * Remove active state
     */

    document
      .querySelectorAll(
        ".match-card, .highlight-card"
      )
      .forEach(function (item) {

        item.classList.remove(
          "active"
        );

      });


    button.classList.add(
      "active"
    );


    /*
     * Update NOW SHOWING
     */

    if (nowShowing) {

      nowShowing.textContent =
        name;

    }


    /*
     * Update status
     */

    if (screenStatus) {

      screenStatus.textContent =
        type === "match"
          ? "LIVE"
          : "HIGHLIGHT";

    }


    /*
     * No URL
     */

    if (!url) {

      if (screenFrame) {

        screenFrame.src =
          "about:blank";

      }


      if (screenPlaceholder) {

        screenPlaceholder.style.display =
          "flex";

      }


      return;

    }


    /*
     * Hide placeholder
     */

    if (screenPlaceholder) {

      screenPlaceholder.style.display =
        "none";

    }


    /*
     * Fade screen during loading
     */

    if (screenFrame) {

      screenFrame.style.opacity =
        "0.25";


      screenFrame.src =
        "about:blank";


      setTimeout(function () {

        screenFrame.src =
          url;

        screenFrame.style.opacity =
          "1";

      }, 150);

    }


    /*
     * Move user to screen
     */

    if (screenPlayer) {

      screenPlayer.scrollIntoView({

        behavior: "smooth",

        block: "start"

      });

    }

  }


  /* =========================================================
     MATCH EVENTS
     ========================================================= */

  function attachMatchEvents() {

    if (!matchList) {
      return;
    }


    matchList
      .querySelectorAll(".match-card")
      .forEach(function (button) {

        button.addEventListener(
          "click",
          function () {

            loadItem(
              this,
              "match"
            );

          }
        );

      });

  }


  /* =========================================================
     HIGHLIGHT EVENTS
     ========================================================= */

  function attachHighlightEvents() {

    if (!highlightList) {
      return;
    }


    highlightList
      .querySelectorAll(".highlight-card")
      .forEach(function (button) {

        button.addEventListener(
          "click",
          function () {

            loadItem(
              this,
              "highlight"
            );

          }
        );

      });

  }


  /* =========================================================
     SHOW FOOTBALL
     ========================================================= */

  function showFootball() {

    currentMode =
      "football";


    if (footballContent) {

      footballContent.hidden =
        false;

    }


    if (highlightsContent) {

      highlightsContent.hidden =
        true;

    }


    if (footballButton) {

      footballButton.classList.add(
        "active"
      );

    }


    if (highlightsButton) {

      highlightsButton.classList.remove(
        "active"
      );

    }


    if (navFootball) {

      navFootball.classList.add(
        "active"
      );

    }


    if (navHighlights) {

      navHighlights.classList.remove(
        "active"
      );

    }


    updateCounts();

    closeMobileMenu();

  }


  /* =========================================================
     SHOW HIGHLIGHTS
     ========================================================= */

  function showHighlights() {

    currentMode =
      "highlights";


    if (footballContent) {

      footballContent.hidden =
        true;

    }


    if (highlightsContent) {

      highlightsContent.hidden =
        false;

    }


    if (footballButton) {

      footballButton.classList.remove(
        "active"
      );

    }


    if (highlightsButton) {

      highlightsButton.classList.add(
        "active"
      );

    }


    if (navFootball) {

      navFootball.classList.remove(
        "active"
      );

    }


    if (navHighlights) {

      navHighlights.classList.add(
        "active"
      );

    }


    updateCounts();

    closeMobileMenu();

  }


  /* =========================================================
     FOOTBALL BUTTON
     ========================================================= */

  if (footballButton) {

    footballButton.addEventListener(
      "click",
      function () {

        showFootball();


        if (footballContent) {

          footballContent.scrollIntoView({

            behavior: "smooth",

            block: "start"

          });

        }

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

        showHighlights();


        if (highlightsContent) {

          highlightsContent.scrollIntoView({

            behavior: "smooth",

            block: "start"

          });

        }

      }
    );

  }


  /* =========================================================
     NAVIGATION
     ========================================================= */

  if (navFootball) {

    navFootball.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        showFootball();

      }
    );

  }


  if (navHighlights) {

    navHighlights.addEventListener(
      "click",
      function (event) {

        event.preventDefault();

        showHighlights();

      }
    );

  }


  /* =========================================================
     UPDATE COUNTS
     ========================================================= */

  function updateCounts() {

    const matches =
      matchList
        ? matchList.querySelectorAll(
            ".match-card"
          ).length
        : 0;


    const highlights =
      highlightList
        ? highlightList.querySelectorAll(
            ".highlight-card"
          ).length
        : 0;


    if (matchCount) {

      matchCount.textContent =
        matches;

    }


    if (highlightCount) {

      highlightCount.textContent =
        highlights;

    }


    if (footballEmpty) {

      footballEmpty.hidden =
        matches !== 0;

    }


    if (highlightEmpty) {

      highlightEmpty.hidden =
        highlights !== 0;

    }

  }


  /* =========================================================
     FULLSCREEN
     ========================================================= */

  if (fullscreenButton) {

    fullscreenButton.addEventListener(
      "click",
      function () {

        if (!screenPlayer) {
          return;
        }


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

  function closeMobileMenu() {

    if (!mainNav) {
      return;
    }


    mainNav.classList.remove(
      "open"
    );


    if (menuToggle) {

      menuToggle.setAttribute(
        "aria-expanded",
        "false"
      );

    }

  }


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
      .forEach(function (link) {

        link.addEventListener(
          "click",
          function () {

            closeMobileMenu();

          }
        );

      });

  }


  /* =========================================================
     CLICK OUTSIDE MOBILE MENU
     ========================================================= */

  document.addEventListener(
    "click",
    function (event) {

      if (
        !mainNav ||
        !menuToggle
      ) {
        return;
      }


      if (
        mainNav.contains(
          event.target
        ) ||
        menuToggle.contains(
          event.target
        )
      ) {

        return;

      }


      closeMobileMenu();

    }
  );


  /* =========================================================
     ESCAPE
     ========================================================= */

  document.addEventListener(
    "keydown",
    function (event) {

      if (
        event.key === "Escape"
      ) {

        closeMobileMenu();

      }

    }
  );


  /* =========================================================
     INITIALIZE
     ========================================================= */

  applyMatchLinks();

  applyHighlightLinks();

  attachMatchEvents();

  attachHighlightEvents();

  updateCounts();

  resetScreen();

  showFootball();


  /* =========================================================
     OPTIONAL GLOBAL CONTROLS
     ========================================================= */

  window.DeeprowssFootball = {

    showFootball:
      showFootball,

    showHighlights:
      showHighlights,

    loadItem:
      loadItem,

    resetScreen:
      resetScreen,

    updateCounts:
      updateCounts

  };


});
