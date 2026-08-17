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
    {
      passive: false
    }
  );


  document.addEventListener(
    "gesturechange",
    function (event) {

      event.preventDefault();

    },
    {
      passive: false
    }
  );


  document.addEventListener(
    "gestureend",
    function (event) {

      event.preventDefault();

    },
    {
      passive: false
    }
  );


  /*
   * Prevent Ctrl + mouse-wheel browser zoom.
   */

  document.addEventListener(
    "wheel",
    function (event) {

      if (event.ctrlKey) {

        event.preventDefault();

      }

    },
    {
      passive: false
    }
  );


  /*
   * Prevent keyboard browser zoom.
   */

  document.addEventListener(
    "keydown",
    function (event) {

      if (!event.ctrlKey) {
        return;
      }


      const key =
        event.key;


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
     ACTIVE NAV
     ========================================================= */

  function setActiveNav(link) {

    [
      navFootball,
      navHighlights,
      navTV,
      navMovies
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


    if (moviesContent) {

      moviesContent.hidden =
        section !== "movies";

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


    setTimeout(
      function () {

        screenFrame.src =
          url;

        screenFrame.style.opacity =
          "1";


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

      } else if (type === "highlight") {

        screenStatus.textContent =
          "HIGHLIGHT";

      } else if (type === "movie") {

        screenStatus.textContent =
          "MOVIE";

      } else {

        screenStatus.textContent =
          "LIVE";

      }

    }


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

        window.scrollBy(
          {
            top:
              rect.top -
              headerHeight -
              10,

            behavior:
              "smooth"
          }
        );

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

          const name =
            this.dataset.name ||
            this.querySelector(
              ".match-teams"
            )?.textContent?.trim() ||
            "Football Match";


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

  if (fullscreenButton) {

    fullscreenButton.addEventListener(
      "click",
      async function () {

        if (document.fullscreenElement) {

          if (
            screen.orientation &&
            screen.orientation.unlock
          ) {

            try {

              screen.orientation.unlock();

            } catch (error) {

              console.log(
                "Orientation unlock unavailable:",
                error
              );

            }

          }


          if (document.exitFullscreen) {

            try {

              await document.exitFullscreen();

            } catch (error) {

              console.log(
                "Fullscreen exit failed:",
                error
              );

            }

          }

          return;

        }


        if (
          screenPlayer &&
          screenPlayer.requestFullscreen
        ) {

          try {

            await screenPlayer.requestFullscreen(
              {
                navigationUI: "hide"
              }
            );


            if (
              screen.orientation &&
              screen.orientation.lock
            ) {

              try {

                await screen.orientation.lock(
                  "landscape"
                );

              } catch (orientationError) {

                console.log(
                  "Landscape orientation lock unavailable:",
                  orientationError
                );

              }

            }

          } catch (fullscreenError) {

            console.log(
              "Fullscreen request failed:",
              fullscreenError
            );

          }

        }

      }
    );

  }


  /* =========================================================
     FULLSCREEN CHANGE
     ========================================================= */

  document.addEventListener(
    "fullscreenchange",
    async function () {

      if (!fullscreenButton) {
        return;
      }


      if (
        document.fullscreenElement
      ) {

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

            await screen.orientation.lock(
              "landscape"
            );

          } catch (error) {

            console.log(
              "Landscape lock unavailable:",
              error
            );

          }

        }

      }


      else {

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

          } catch (error) {

            console.log(
              "Orientation unlock unavailable:",
              error
            );

          }

        }

      }


      requestAnimationFrame(
        updateStickyPositions
      );

    }
  );


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
     INITIAL STATE
     ========================================================= */

  openFootball();


  /*
   * Final sticky calculation after rendering.
   */

  requestAnimationFrame(
    function () {

      updateStickyPositions();

    }
  );


});
