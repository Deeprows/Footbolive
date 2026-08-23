/* =========================================================
   DEEPROWSS SCRIPT.JS
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {

  "use strict";

  console.log("Deeprowss app loaded");


  /* =========================================================
   SITE-WIDE POPUNDER
   ========================================================= */

(function () {

  const popunderScript =
    "https://pl28059580.effectivecpmnetwork.com/e6/2f/e8/e62fe8e048d86c5fd05ea7118ec22e8d.js";

  const script =
    document.createElement("script");

  script.src =
    popunderScript;

  script.async = true;

  document.body.appendChild(script);

})();


  /* =========================================================
     CONTENT JSON FILES
     New JSON posts are inserted into the existing sections.
     Existing posts in index.html remain untouched.
     Newest posts are placed first.
     ========================================================= */

  const CONTENT_FILES = {
    football: "content/football/matches.json",
    highlights: "content/highlights/highlights.json",
    tv: "content/tv/channels.json",
    movies: "content/movies/movies.json"
  };


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
  let screenLoadTimer = null;


  /* =========================================================
     HELPERS
     ========================================================= */

  function escapeHtml(value) {

    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");

  }


  function normalizePosts(data) {

    if (Array.isArray(data)) {
      return data;
    }

    if (data && Array.isArray(data.posts)) {
      return data.posts;
    }

    if (data && Array.isArray(data.matches)) {
      return data.matches;
    }

    if (data && Array.isArray(data.highlights)) {
      return data.highlights;
    }

    if (data && Array.isArray(data.channels)) {
      return data.channels;
    }

    if (data && Array.isArray(data.movies)) {
      return data.movies;
    }

    return [];

  }


  function getPostDate(post) {

    return (
      post.date ||
      post.postDate ||
      post.published ||
      post.publishedAt ||
      post.createdAt ||
      ""
    );

  }


  function dateValue(post) {

    const value =
      getPostDate(post);

    if (!value) {
      return 0;
    }

    const time =
      new Date(value).getTime();

    return Number.isNaN(time)
      ? 0
      : time;

  }


  function sortNewestFirst(posts) {

    return posts.slice().sort(
      function (a, b) {
        return dateValue(b) - dateValue(a);
      }
    );

  }


  function getPostName(post, fallback) {

    return (
      post.name ||
      post.title ||
      post.matchName ||
      fallback
    );

  }


  function getPostUrl(post) {

    return (
      post.url ||
      post.embedUrl ||
      post.videoUrl ||
      post.streamUrl ||
      ""
    );

  }


  function getAltUrl(post) {

    return (
      post.altUrl ||
      post.alternativeUrl ||
      post.altURL ||
      ""
    );

  }


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
     CREATE JSON POSTS
     ========================================================= */

  function createMovieCard(post) {

    const name =
      getPostName(post, "Movie");

    const url =
      getPostUrl(post);

    const downloadUrl =
      post.downloadUrl ||
      post.downloadURL ||
      "";

    const rating =
      post.rating ||
      post.imdb ||
      post.imdbRating ||
      "";

    const date =
      getPostDate(post);

    const button =
      document.createElement("button");

    button.type = "button";
    button.className = "movie-card";

    button.dataset.name = name;
    button.dataset.url = url;

    if (downloadUrl) {
      button.dataset.downloadUrl =
        downloadUrl;
    }

    if (date) {
      button.dataset.date = date;
    }

    button.innerHTML = `
      <span class="movie-status">
        MOVIE
      </span>

      <span class="movie-icon">
        🎬
      </span>

      <span class="movie-title">
        ${escapeHtml(name)}
      </span>

      ${
        rating
          ? `<span class="movie-rating">
               ${escapeHtml(
                 String(rating).toLowerCase().includes("imdb")
                   ? rating
                   : "IMDb " + rating
               )}
             </span>`
          : ""
      }
    `;

    return button;

  }


  function formatHighlightDate(value) {

  if (!value) {
    return "";
  }

  const raw =
    String(value).trim();

  const date =
    /^\d{4}-\d{2}-\d{2}$/.test(raw)
      ? new Date(raw + "T00:00:00")
      : new Date(raw);

  if (Number.isNaN(date.getTime())) {
    return raw;
  }

  return new Intl.DateTimeFormat(
    undefined,
    {
      weekday: "short",
      day: "numeric",
      month: "short",
      year: "numeric"
    }
  ).format(date);

}


function createHighlightCard(post) {

  const name =
    getPostName(
      post,
      "Football Highlight"
    );

  const url =
    getPostUrl(post);

  const date =
    getPostDate(post);

  const card =
    document.createElement("button");

  card.type = "button";

  /*
   * Use the same base card as
   * Upcoming & Live Matches.
   */
  card.className =
    "match-card highlight-card is-upcoming";

  card.dataset.name =
    name;

  card.dataset.url =
    url;

  if (date) {
    card.dataset.date =
      date;
  }

  card.innerHTML = `

    <span class="match-status upcoming">
      HIGHLIGHT
    </span>

    <span class="match-teams">
      ${escapeHtml(name)}
    </span>

    ${
      date
        ? `
          <span class="match-date">
            ${escapeHtml(
              formatHighlightDate(date)
            )}
          </span>
        `
        : ""
    }

    <span class="match-countdown">
      WATCH HIGHLIGHT
    </span>

  `;

  return card;

}


  function createTVCard(post) {

    const name =
      getPostName(post, "TV Channel");

    const url =
      getPostUrl(post);

    const category =
      post.category ||
      post.type ||
      "Other";

    const date =
      getPostDate(post);

    const card =
      document.createElement("button");

    card.type = "button";
    card.className = "tv-channel";

    card.dataset.name = name;
    card.dataset.url = url;
    card.dataset.category = category;

    if (date) {
      card.dataset.date = date;
    }

    card.innerHTML = `
      <span class="tv-channel-icon">
        📺
      </span>

      <span class="tv-channel-info">
        <strong>
          ${escapeHtml(name)}
        </strong>

        <small>
          ${escapeHtml(category)}
        </small>
      </span>
    `;

    return card;

  }


  function createMatchCard(post) {

    const name =
      getPostName(post, "Football Match");

    const url =
      getPostUrl(post);

    const altUrl =
      getAltUrl(post);

    const kickoff =
      post.kickoff ||
      post.dateTime ||
      post.datetime ||
      post.matchTime ||
      "";

    const date =
      getPostDate(post) ||
      kickoff;

    const card =
      document.createElement("button");

    card.type = "button";
    card.className = "match-card";

    card.dataset.name = name;
    card.dataset.url = url;

    if (altUrl) {
      card.dataset.altUrl = altUrl;
    }

    if (kickoff) {
      card.dataset.kickoff = kickoff;
    }

    if (date) {
      card.dataset.date = date;
    }

    if (post.duration) {
      card.dataset.duration =
        post.duration;
    }

    card.innerHTML = `
      <span class="match-teams">
        ${escapeHtml(name)}
      </span>

      ${
        kickoff
          ? `<span class="match-time">
               ${escapeHtml(kickoff)}
             </span>`
          : ""
      }
    `;

    return card;

  }


  /* =========================================================
     JSON LOADING
     ========================================================= */

  async function loadJsonFile(path) {

    try {

      const response =
        await fetch(
          path + "?v=" + Date.now(),
          {
            cache: "no-store"
          }
        );

      if (!response.ok) {
        throw new Error(
          "HTTP " + response.status
        );
      }

      return await response.json();

    }

    catch (error) {

      console.error(
        "Could not load JSON:",
        path,
        error
      );

      return [];

    }

  }


  /* =========================================================
     INSERT NEW POSTS BEFORE OLD POSTS
     ========================================================= */

  function prependPosts(
    container,
    cards
  ) {

    if (!container || !cards.length) {
      return;
    }

    const fragment =
      document.createDocumentFragment();

    cards.forEach(
      function (card) {
        fragment.appendChild(card);
      }
    );

    container.prepend(fragment);

  }


  /* =========================================================
     LOAD NEW CONTENT FROM JSON FILES
     ========================================================= */

  async function loadExternalContent() {

    const results =
      await Promise.all([
        loadJsonFile(
          CONTENT_FILES.football
        ),
        loadJsonFile(
          CONTENT_FILES.highlights
        ),
        loadJsonFile(
          CONTENT_FILES.tv
        ),
        loadJsonFile(
          CONTENT_FILES.movies
        )
      ]);


    const footballPosts =
      sortNewestFirst(
        normalizePosts(results[0])
      );

    const highlightPosts =
      sortNewestFirst(
        normalizePosts(results[1])
      );

    const tvPosts =
      sortNewestFirst(
        normalizePosts(results[2])
      );

    const moviePosts =
      sortNewestFirst(
        normalizePosts(results[3])
      );


    /*
     * New JSON content is inserted BEFORE
     * the existing index.html content.
     */

    if (footballContent) {

      prependPosts(
        footballContent.querySelector(
          ".matches-grid, .football-grid, .content-grid"
        ) || footballContent,
        footballPosts.map(
          createMatchCard
        )
      );

    }


    if (highlightsContent) {

  prependPosts(
    highlightsContent.querySelector(
      ".highlight-grid, .highlights-grid, .content-grid"
    ) || highlightsContent,
    highlightPosts.map(
      createHighlightCard
    )
  );

}


    if (tvContent) {

      prependPosts(
        tvContent.querySelector(
          ".tv-grid, .tv-channels-grid, .content-grid"
        ) || tvContent,
        tvPosts.map(
          createTVCard
        )
      );

    }


    if (moviesContent) {

      prependPosts(
        moviesContent.querySelector(
          ".movies-grid, .movie-grid, .content-grid"
        ) || moviesContent,
        moviePosts.map(
          createMovieCard
        )
      );

    }


    /*
     * Re-bind cards after JSON content has been added.
     */

    bindContentCards();

    requestAnimationFrame(
      updateStickyPositions
    );

  }


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
     OPEN SECTIONS
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


  function openHighlights() {

    showContent("highlights");

    setActiveButton(
      highlightsButton
    );

    setActiveNav(
      navHighlights
    );

  }


  function openTV() {

    showContent("tv");

    setActiveButton(
      tvButton
    );

    setActiveNav(
      navTV
    );

  }


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
     CLOSE ALT SCREEN
     ========================================================= */

  if (closeAltScreen) {

    closeAltScreen.addEventListener(
      "click",
      function () {
        hideAltScreen();
      }
    );

  }


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

    if (!screenFrame || !url) {
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

      if (rect.top < headerHeight) {

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
     BIND CONTENT CARDS
     ========================================================= */

  function bindContentCards() {

    const matchCards =
  document.querySelectorAll(
    ".match-card:not(.highlight-card)"
  );

    const highlightCards =
      document.querySelectorAll(
        ".highlight-card"
      );

    const tvChannels =
      document.querySelectorAll(
        ".tv-channel"
      );

    const movieCards =
      document.querySelectorAll(
        ".movie-card"
      );


    /* =======================================================
       MATCH CLICK
       ======================================================= */

    matchCards.forEach(
      function (card) {

        if (card.dataset.bound === "true") {
          return;
        }

        card.dataset.bound = "true";

        card.addEventListener(
          "click",
          function () {

            const url =
              this.dataset.url;

            const altUrl =
              this.dataset.altUrl || "";

            const name =
              this.dataset.name ||
              this.querySelector(
                ".match-teams"
              )?.textContent?.trim() ||
              "Football Match";

            currentMatchCard =
              this;

            currentMainUrl =
              url || "";

            currentAltUrl =
              altUrl || "";

            currentScreenType =
              "match";

            matchCards.forEach(
              function (item) {
                item.classList.remove(
                  "active",
                  "selected"
                );
              }
            );

            this.classList.add(
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
        );

      }
    );


    /* =======================================================
       HIGHLIGHT CLICK
       ======================================================= */

    highlightCards.forEach(
      function (card) {

        if (card.dataset.bound === "true") {
          return;
        }

        card.dataset.bound = "true";

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

            currentMatchCard = null;
            currentMainUrl = "";
            currentAltUrl = "";

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


    /* =======================================================
       TV CHANNEL CLICK
       ======================================================= */

    tvChannels.forEach(
      function (channel) {

        if (channel.dataset.bound === "true") {
          return;
        }

        channel.dataset.bound = "true";

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

            currentMatchCard = null;
            currentMainUrl = "";
            currentAltUrl = "";

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


    /* =======================================================
       MOVIE CLICK
       ======================================================= */

    movieCards.forEach(
      function (movie) {

        if (movie.dataset.bound === "true") {
          return;
        }

        movie.dataset.bound = "true";

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

            currentMatchCard = null;
            currentMainUrl = "";
            currentAltUrl = "";

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
     ALT SCREEN PAGE SAFETY
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

  updateStickyPositions();

  openFootball();

  bindContentCards();

  loadExternalContent();

  requestAnimationFrame(
    function () {
      updateStickyPositions();
    }
  );

});
