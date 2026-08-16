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
     INITIAL STATE
     ========================================================= */

  let currentMode = "football";

  let currentItem = null;


  /* =========================================================
     SCREEN
     ========================================================= */

  function resetScreen() {

    if (!screenFrame) {
      return;
    }

    screenFrame.style.opacity = "0";

    screenFrame.src = "about:blank";

    setTimeout(function () {

      screenFrame.style.opacity = "1";

    }, 150);


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

  }


  function loadItem(button, type) {

    if (!button) {
      return;
    }


    const name =
      button.dataset.name || "Selected content";


    const url =
      button.dataset.url || "";


    currentItem = {
      name: name,
      url: url,
      type: type
    };


    /*
     * Remove active state from all
     * football and highlight buttons.
     */

    document
      .querySelectorAll(
        ".match-card, .highlight-card"
      )
      .forEach(function (item) {

        item.classList.remove("active");

      });


    button.classList.add("active");


    /*
     * Update screen title.
     */

    if (nowShowing) {

      nowShowing.textContent =
        name;

    }


    /*
     * Update screen status.
     */

    if (screenStatus) {

      screenStatus.textContent =
        type === "match"
          ? "LIVE"
          : "HIGHLIGHT";

    }


    /*
     * If no URL has been added yet,
     * keep the player blank.
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
     * Hide placeholder.
     */

    if (screenPlaceholder) {

      screenPlaceholder.style.display =
        "none";

    }


    /*
     * Fade screen while changing
     * content.
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
     * Bring the screen into view.
     */

    if (screenPlayer) {

      screenPlayer.scrollIntoView({
        behavior: "smooth",
        block: "start"
      });

    }

  }


  /* =========================================================
     FOOTBALL MATCH CLICKING
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
     HIGHLIGHT CLICKING
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
     APP BUTTONS
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


        if (document.fullscreenElement) {

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
     FULLSCREEN BUTTON ICON
     ========================================================= */

  document.addEventListener(
    "fullscreenchange",
    function () {

      if (!fullscreenButton) {
        return;
      }


      if (document.fullscreenElement) {

        fullscreenButton.textContent =
          "⛶";

      } else {

        fullscreenButton.textContent =
          "⛶";

      }

    }
  );


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

        const open =
          mainNav.classList.toggle(
            "open"
          );


        menuToggle.setAttribute(
          "aria-expanded",
          open
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
     CLOSE MOBILE MENU WHEN CLICKING
     OUTSIDE
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
        mainNav.contains(event.target) ||
        menuToggle.contains(event.target)
      ) {

        return;

      }


      closeMobileMenu();

    }
  );


  /* =========================================================
     ESCAPE KEY
     ========================================================= */

  document.addEventListener(
    "keydown",
    function (event) {

      if (event.key === "Escape") {

        closeMobileMenu();

      }

    }
  );


  /* =========================================================
     PREVENT EMPTY LINKS FROM JUMPING
     ========================================================= */

  document
    .querySelectorAll(
      'a[href="#"]'
    )
    .forEach(function (link) {

      link.addEventListener(
        "click",
        function (event) {

          event.preventDefault();

        }
      );

    });


  /* =========================================================
     INITIALIZE
     ========================================================= */

  attachMatchEvents();

  attachHighlightEvents();

  updateCounts();

  resetScreen();

  showFootball();


  /* =========================================================
     EXPOSE OPTIONAL APP FUNCTIONS
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
