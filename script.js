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

  /*
   * The screen and app navigation are both sticky.
   *
   * Because the player height changes between desktop,
   * tablet and mobile, calculate the real height instead
   * of relying on a hard-coded value.
   */

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


  /*
   * Initial calculation.
   */

  updateStickyPositions();


  /*
   * Recalculate when the browser changes size.
   */

  window.addEventListener(
    "resize",
    updateStickyPositions
  );


  /*
   * ResizeObserver gives more accurate updates when
   * the iframe/player dimensions change.
   */

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


    /*
     * Recalculate sticky layout because hidden
     * sections can change page dimensions.
     */

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


    /*
     * Update current title.
     */

    if (nowShowing) {

      nowShowing.textContent =
        name || "Now Playing";

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
     * Dim iframe while changing content.
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


        /*
         * Recalculate sticky position
         * after iframe begins loading.
         */

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
     *
     * The sticky player remains in position while
     * the user browses the page.
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


          /*
           * Active highlight.
           */

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
           * Load channel.
           */

          loadScreen(
            url,
            name,
            "tv"
          );


          /*
           * Keep TV section active.
           */

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


          /*
           * Remove active state
           * from all movies.
           */

          movieCards.forEach(
            function (item) {

              item.classList.remove(
                "active"
              );

            }
          );


          /*
           * Mark selected movie.
           */

          this.classList.add(
            "active"
          );


          /*
           * Load movie.
           */

          loadScreen(
            url,
            name,
            "movie"
          );


          /*
           * Keep Movies section active.
           */

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
      function () {

        /*
         * Exit fullscreen if already fullscreen.
         */

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


        /*
         * Enter fullscreen.
         */

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
     FULLSCREEN CHANGE
     ========================================================= */

  document.addEventListener(
    "fullscreenchange",
    function () {

      if (!fullscreenButton) {
        return;
      }


      if (
        document.fullscreenElement
      ) {

        fullscreenButton.textContent =
          "⛶";

        fullscreenButton.setAttribute(
          "aria-label",
          "Exit fullscreen"
        );

      } else {

        fullscreenButton.textContent =
          "⛶";

        fullscreenButton.setAttribute(
          "aria-label",
          "Enter fullscreen"
        );

      }

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
     MATCH TIME / TIMEZONE SYSTEM
     ========================================================= */

  /*
   * This system supports match cards that contain:
   *
   * data-start="2026-08-17T19:00:00"
   *
   * OR
   *
   * data-start="2026-08-17T19:00:00+01:00"
   *
   * OR
   *
   * data-start="2026-08-17T19:00:00Z"
   *
   * The browser's local timezone is used automatically.
   *
   * If the HTML does not contain data-start,
   * the existing card content is left untouched.
   */


  const userTimeZone =
    Intl.DateTimeFormat().resolvedOptions().timeZone;


  function getMatchStart(card) {

    if (!card) {
      return null;
    }


    const raw =
      card.dataset.start ||
      card.dataset.datetime ||
      card.dataset.dateTime;


    if (!raw) {
      return null;
    }


    const date =
      new Date(raw);


    if (
      Number.isNaN(
        date.getTime()
      )
    ) {

      return null;

    }


    return date;

  }


  function formatLocalDate(date) {

    return new Intl.DateTimeFormat(
      undefined,
      {
        weekday: "short",
        day: "numeric",
        month: "short",
        timeZone: userTimeZone
      }
    ).format(date);

  }


  function formatLocalTime(date) {

    return new Intl.DateTimeFormat(
      undefined,
      {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
        timeZone: userTimeZone
      }
    ).format(date);

  }


  function formatDuration(milliseconds) {

    let totalSeconds =
      Math.max(
        0,
        Math.floor(
          milliseconds / 1000
        )
      );


    const days =
      Math.floor(
        totalSeconds / 86400
      );


    totalSeconds %=
      86400;


    const hours =
      Math.floor(
        totalSeconds / 3600
      );


    totalSeconds %=
      3600;


    const minutes =
      Math.floor(
        totalSeconds / 60
      );


    const seconds =
      totalSeconds % 60;


    if (days > 0) {

      return (
        days +
        "d " +
        String(hours).padStart(2, "0") +
        "h"
      );

    }


    if (hours > 0) {

      return (
        hours +
        "h " +
        String(minutes).padStart(2, "0") +
        "m"
      );

    }


    if (minutes > 0) {

      return (
        minutes +
        "m " +
        String(seconds).padStart(2, "0") +
        "s"
      );

    }


    return (
      seconds +
      "s"
    );

  }


  function updateMatchCard(card) {

    const start =
      getMatchStart(card);


    /*
     * No timing data means do nothing.
     */

    if (!start) {
      return;
    }


    const now =
      new Date();


    const difference =
      start.getTime() -
      now.getTime();


    const statusElement =
      card.querySelector(
        ".match-status"
      );


    const countdownElement =
      card.querySelector(
        ".match-countdown"
      );


    const dateElement =
      card.querySelector(
        ".match-date"
      );


    const timeElement =
      card.querySelector(
        ".match-time"
      );


    /*
     * Always update date/time using
     * the visitor's local timezone.
     */

    if (dateElement) {

      dateElement.textContent =
        formatLocalDate(start);

    }


    if (timeElement) {

      timeElement.textContent =
        formatLocalTime(start);

    }


    /*
     * UPCOMING
     */

    if (difference > 0) {

      card.classList.remove(
        "is-live",
        "is-ended"
      );

      card.classList.add(
        "is-upcoming"
      );


      if (statusElement) {

        statusElement.classList.remove(
          "live",
          "ended"
        );

        statusElement.classList.add(
          "upcoming"
        );

        statusElement.textContent =
          "UPCOMING";

      }


      if (countdownElement) {

        countdownElement.textContent =
          "Starts in " +
          formatDuration(
            difference
          );

      }


      return;

    }


    /*
     * LIVE
     *
     * Without a match duration/end time,
     * the card switches to LIVE after kickoff.
     */

    const endRaw =
      card.dataset.end ||
      card.dataset.endTime;


    if (endRaw) {

      const end =
        new Date(endRaw);


      if (
        !Number.isNaN(
          end.getTime()
        ) &&
        now.getTime() >= end.getTime()
      ) {

        setEndedState(
          card,
          statusElement,
          countdownElement
        );

        return;

      }

    }


    setLiveState(
      card,
      statusElement,
      countdownElement,
      difference
    );

  }


  function setLiveState(
    card,
    statusElement,
    countdownElement,
    difference
  ) {

    card.classList.remove(
      "is-upcoming",
      "is-ended"
    );

    card.classList.add(
      "is-live"
    );


    if (statusElement) {

      statusElement.classList.remove(
        "upcoming",
        "ended"
      );

      statusElement.classList.add(
        "live"
      );

      statusElement.textContent =
        "LIVE";

    }


    if (countdownElement) {

      const elapsed =
        Math.abs(difference);


      countdownElement.textContent =
        "Live • " +
        formatDuration(
          elapsed
        );

    }

  }


  function setEndedState(
    card,
    statusElement,
    countdownElement
  ) {

    card.classList.remove(
      "is-upcoming",
      "is-live"
    );

    card.classList.add(
      "is-ended"
    );


    if (statusElement) {

      statusElement.classList.remove(
        "upcoming",
        "live"
      );

      statusElement.classList.add(
        "ended"
      );

      statusElement.textContent =
        "ENDED";

    }


    if (countdownElement) {

      countdownElement.textContent =
        "Match ended";

    }

  }


  function updateAllMatches() {

    matchCards.forEach(
      function (card) {

        updateMatchCard(card);

      }
    );

  }


  /*
   * Run immediately.
   */

  updateAllMatches();


  /*
   * Keep countdowns accurate.
   */

  setInterval(
    updateAllMatches,
    1000
  );


  /* =========================================================
     INITIAL STATE
     ========================================================= */

  openFootball();


  /*
   * Final sticky calculation after everything
   * has finished rendering.
   */

  requestAnimationFrame(
    function () {

      updateStickyPositions();

      updateAllMatches();

    }
  );


});
