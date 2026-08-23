/* =========================================================
   DEEPROWSS SCRIPT.JS
   Corrected version:
   - Keeps one main screen/iframe
   - Fixes shared links
   - Fixes escaped/broken JavaScript syntax
   - Keeps JSON loading and existing card behavior
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

    const script = document.createElement("script");
    script.src = popunderScript;
    script.async = true;
    document.body.appendChild(script);
  })();

  /* =========================================================
     CONTENT JSON FILES
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
  document.addEventListener("gesturestart", e => e.preventDefault(), { passive: false });
  document.addEventListener("gesturechange", e => e.preventDefault(), { passive: false });
  document.addEventListener("gestureend", e => e.preventDefault(), { passive: false });

  document.addEventListener("wheel", function (event) {
    if (event.ctrlKey) event.preventDefault();
  }, { passive: false });

  document.addEventListener("keydown", function (event) {
    if (!event.ctrlKey) return;
    if (["+", "=", "-", "0"].includes(event.key)) event.preventDefault();
  });

  /* =========================================================
     DEVELOPER TOOLS / RIGHT-CLICK PROTECTION
     ========================================================= */
  document.addEventListener("contextmenu", e => e.preventDefault());

  document.addEventListener("keydown", function (event) {
    if (event.key === "F12") {
      event.preventDefault();
      return false;
    }

    if (
      event.ctrlKey &&
      event.shiftKey &&
      ["I", "J", "C"].includes(event.key.toUpperCase())
    ) {
      event.preventDefault();
      return false;
    }

    if (event.ctrlKey && ["U", "S"].includes(event.key.toUpperCase())) {
      event.preventDefault();
      return false;
    }
  });

  /* =========================================================
     ELEMENTS
     ========================================================= */
  const siteHeader = document.querySelector(".site-header");
  const screenSection = document.querySelector(".screen-section");
  const screenFrame = document.getElementById("screenFrame");
  const screenPlaceholder = document.getElementById("screenPlaceholder");
  const screenStatus = document.getElementById("screenStatus");
  const nowShowing = document.getElementById("nowShowing");
  const fullscreenButton = document.getElementById("fullscreenButton");
  const screenPlayer = document.getElementById("screenPlayer");

  const footballScreenControls =
    document.getElementById("footballScreenControls");
  const mainScreenButton =
    document.getElementById("mainScreenButton");
  const altScreenButton =
    document.getElementById("altScreenButton");

  const altScreenOverlay =
    document.getElementById("altScreenOverlay");
  const altScreenFrame =
    document.getElementById("altScreenFrame");
  const altScreenMatch =
    document.getElementById("altScreenMatch");
  const closeAltScreen =
    document.getElementById("closeAltScreen");

  const footballButton =
    document.getElementById("footballButton");
  const highlightsButton =
    document.getElementById("highlightsButton");
  const tvButton =
    document.getElementById("tvButton");
  const moviesButton =
    document.getElementById("moviesButton");

  const footballContent =
    document.getElementById("footballContent");
  const highlightsContent =
    document.getElementById("highlightsContent");
  const tvContent =
    document.getElementById("tvContent");
  const moviesContent =
    document.getElementById("moviesContent");

  const navFootball =
    document.getElementById("navFootball");
  const navHighlights =
    document.getElementById("navHighlights");
  const navTV =
    document.getElementById("navTV");
  const navMovies =
    document.getElementById("navMovies");

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

  let hlsVideo = null;
  let hlsInstance = null;
  let hlsUnmuteButton = null;
  let hlsLibraryPromise = null;

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
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.posts)) return data.posts;
    if (data && Array.isArray(data.matches)) return data.matches;
    if (data && Array.isArray(data.highlights)) return data.highlights;
    if (data && Array.isArray(data.channels)) return data.channels;
    if (data && Array.isArray(data.movies)) return data.movies;
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
    const value = getPostDate(post);
    if (!value) return 0;

    const time = new Date(value).getTime();
    return Number.isNaN(time) ? 0 : time;
  }

  function sortNewestFirst(posts) {
    return posts.slice().sort((a, b) => dateValue(b) - dateValue(a));
  }

  function getPostName(post, fallback) {
    return post.name || post.title || post.matchName || fallback;
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
    if (!url) return false;

    const normalized = String(url).trim();

    if (!normalized) return false;

    if (
      normalized.indexOf("PASTE-") === 0 ||
      normalized.indexOf("ALT-SCREEN-URL") !== -1
    ) {
      return false;
    }

    return true;
  }

  /* =========================================================
     SHARE LINK
     ========================================================= */
  function getSharePageUrl(card, type) {
    if (!card) return window.location.href;

    const explicitShareUrl =
      card.dataset.shareUrl ||
      card.dataset.share ||
      "";

    if (explicitShareUrl) return explicitShareUrl;

    const name =
      card.dataset.name || "Deeprowss";

    const contentUrl =
      card.dataset.url || "";

    const cleanUrl =
      window.location.href
        .split("#")[0]
        .split("?")[0];

    const params = new URLSearchParams();

    params.set("type", type || "content");

    if (contentUrl) {
      params.set("url", contentUrl);
    }

    params.set("name", String(name).trim());

    return cleanUrl + "#share=" + params.toString();
  }

  function getShareTitle(card, type) {
    const name =
      card?.dataset?.name || "Deeprowss";

    const labels = {
      match: "Football Live",
      tv: "TV Channel",
      highlight: "Football Highlight",
      movie: "Movie"
    };

    return (
      name +
      " - " +
      (labels[type] || "Content") +
      " | Deeprowss"
    );
  }

  function getShareText(card, type) {
    const name =
      card?.dataset?.name || "Deeprowss";

    const labels = {
      match: "football match",
      tv: "TV channel",
      highlight: "football highlight",
      movie: "movie"
    };

    return (
      "Watch " +
      name +
      " on Deeprowss. Shared " +
      (labels[type] || "content") +
      " from Deeprowss."
    );
  }

  /* =========================================================
     READ SHARED LINK
     ========================================================= */
  function getSharedContentData() {
    const hash =
      window.location.hash || "";

    if (!hash.startsWith("#share=")) {
      return null;
    }

    try {
      const query =
        hash.substring("#share=".length);

      const params =
        new URLSearchParams(query);

      const type =
        params.get("type") || "";

      const url =
        params.get("url") || "";

      const name =
        params.get("name") || "";

      if (!type && !url && !name) {
        return null;
      }

      return {
        type,
        url,
        name
      };
    } catch (error) {
      console.log(
        "Could not read shared content link:",
        error
      );
      return null;
    }
  }

  function findSharedCard(data) {
    if (!data) return null;

    const type =
      String(data.type || "").toLowerCase();

    const url =
      String(data.url || "").trim();

    const name =
      String(data.name || "").trim();

    let selector =
      ".match-card, .tv-channel, .movie-card";

    if (type === "match") {
      selector =
        ".match-card:not(.highlight-card)";
    } else if (type === "highlight") {
      selector =
        ".highlight-card";
    } else if (type === "tv") {
      selector =
        ".tv-channel";
    } else if (type === "movie") {
      selector =
        ".movie-card";
    }

    const cards =
      document.querySelectorAll(selector);

    if (url) {
      for (const card of cards) {
        if (
          String(card.dataset.url || "").trim() === url
        ) {
          return card;
        }
      }
    }

    if (name) {
      const normalizedName =
        name.toLowerCase();

      for (const card of cards) {
        const cardName =
          String(
            card.dataset.name || ""
          ).trim().toLowerCase();

        if (cardName === normalizedName) {
          return card;
        }
      }
    }

    return null;
  }

  let sharedContentResolveTimer = null;

  function resolveSharedContent(attempt) {
    const data =
      getSharedContentData();

    if (!data) return;

    const card =
      findSharedCard(data);

    if (card) {
      if (sharedContentResolveTimer) {
        clearTimeout(sharedContentResolveTimer);
        sharedContentResolveTimer = null;
      }

      const type =
        String(data.type || "").toLowerCase();

      if (type === "match") {
        openFootball();
      } else if (type === "highlight") {
        openHighlights();
      } else if (type === "tv") {
        openTV();
      } else if (type === "movie") {
        openMovies();
      }

      /*
       * IMPORTANT:
       * Use the existing card click handler.
       * No new iframe/screen is created.
       */
      setTimeout(function () {
        if (document.body.contains(card)) {
          card.click();
        }
      }, 150);

      return;
    }

    const nextAttempt =
      Number(attempt || 0) + 1;

    if (nextAttempt <= 80) {
      sharedContentResolveTimer =
        setTimeout(function () {
          resolveSharedContent(nextAttempt);
        }, 150);
    }
  }

  function handleSharedContentHash() {
    if (sharedContentResolveTimer) {
      clearTimeout(sharedContentResolveTimer);
      sharedContentResolveTimer = null;
    }

    resolveSharedContent(0);
  }

  window.addEventListener(
    "hashchange",
    handleSharedContentHash
  );

  /* =========================================================
     SHARE ACTION
     ========================================================= */
  async function copyShareUrl(url) {
    try {
      if (
        navigator.clipboard &&
        typeof navigator.clipboard.writeText === "function"
      ) {
        await navigator.clipboard.writeText(url);
        alert("Share link copied.");
        return true;
      }
    } catch (error) {
      console.log(
        "Clipboard API failed:",
        error
      );
    }

    try {
      const textarea =
        document.createElement("textarea");

      textarea.value = url;
      textarea.style.position = "fixed";
      textarea.style.left = "-9999px";
      textarea.style.top = "0";

      document.body.appendChild(textarea);

      textarea.focus();
      textarea.select();

      const copied =
        document.execCommand("copy");

      textarea.remove();

      if (copied) {
        alert("Share link copied.");
        return true;
      }
    } catch (error) {
      console.log(
        "Fallback copy failed:",
        error
      );
    }

    return false;
  }

  async function shareContent(card, type) {
    if (!card) return;

    const url =
      getSharePageUrl(card, type);

    const title =
      getShareTitle(card, type);

    const text =
      getShareText(card, type);

    if (
      navigator.share &&
      typeof navigator.share === "function"
    ) {
      try {
        await navigator.share({
          title,
          text,
          url
        });

        return;
      } catch (error) {
        if (
          error &&
          error.name === "AbortError"
        ) {
          return;
        }

        console.log(
          "Native share failed:",
          error
        );
      }
    }

    await copyShareUrl(url);
  }

  function createShareButton(card, type) {
    if (!card) return null;

    const existing =
      card.querySelector(
        ".deeprowss-share-button"
      );

    if (existing) return existing;

    const shareButton =
      document.createElement("span");

    shareButton.className =
      "deeprowss-share-button";

    shareButton.setAttribute(
      "role",
      "button"
    );

    shareButton.setAttribute(
      "tabindex",
      "0"
    );

    shareButton.setAttribute(
      "aria-label",
      "Share " +
      (card.dataset.name || "content")
    );

    shareButton.setAttribute(
      "title",
      "Share"
    );

    shareButton.textContent = "↗";

    Object.assign(
      shareButton.style,
      {
        position: "absolute",
        top: "8px",
        right: "8px",
        width: "34px",
        height: "34px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: "20",
        borderRadius: "50%",
        background: "rgba(0,0,0,.72)",
        color: "#fff",
        fontSize: "20px",
        fontWeight: "700",
        lineHeight: "1",
        cursor: "pointer",
        userSelect: "none",
        WebkitTapHighlightColor: "transparent"
      }
    );

    shareButton.addEventListener(
      "click",
      function (event) {
        event.preventDefault();
        event.stopPropagation();

        shareContent(
          card,
          type
        );
      }
    );

    shareButton.addEventListener(
      "keydown",
      function (event) {
        if (
          event.key === "Enter" ||
          event.key === " "
        ) {
          event.preventDefault();
          event.stopPropagation();

          shareContent(
            card,
            type
          );
        }
      }
    );

    if (
      window.getComputedStyle(card).position ===
      "static"
    ) {
      card.style.position = "relative";
    }

    card.appendChild(shareButton);

    return shareButton;
  }

  function addShareButtons() {
    document
      .querySelectorAll(
        ".match-card:not(.highlight-card)"
      )
      .forEach(function (card) {
        createShareButton(card, "match");
      });

    document
      .querySelectorAll(".highlight-card")
      .forEach(function (card) {
        createShareButton(card, "highlight");
      });

    document
      .querySelectorAll(".tv-channel")
      .forEach(function (card) {
        createShareButton(card, "tv");
      });

    document
      .querySelectorAll(".movie-card")
      .forEach(function (card) {
        createShareButton(card, "movie");
      });
  }

  /* =========================================================
     JSON CARD CREATION
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
          ? `
            <span class="movie-rating">
              ${escapeHtml(
                String(rating)
                  .toLowerCase()
                  .includes("imdb")
                  ? rating
                  : "IMDb " + rating
              )}
            </span>
          `
          : ""
      }
    `;

    return button;
  }

  function formatHighlightDate(value) {
    if (!value) return "";

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

    card.className =
      "match-card highlight-card is-upcoming";

    card.dataset.name = name;
    card.dataset.url = url;

    if (date) {
      card.dataset.date = date;
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
      getPostName(
        post,
        "TV Channel"
      );

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
      getPostName(
        post,
        "Football Match"
      );

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
      card.dataset.altUrl =
        altUrl;
    }

    if (kickoff) {
      card.dataset.kickoff =
        kickoff;
    }

    if (date) {
      card.dataset.date =
        date;
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
          ? `
            <span class="match-time">
              ${escapeHtml(kickoff)}
            </span>
          `
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
    } catch (error) {
      console.error(
        "Could not load JSON:",
        path,
        error
      );

      return [];
    }
  }

  function prependPosts(
    container,
    cards
  ) {
    if (
      !container ||
      !cards.length
    ) {
      return;
    }

    const fragment =
      document.createDocumentFragment();

    cards.forEach(function (card) {
      fragment.appendChild(card);
    });

    container.prepend(fragment);
  }

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

    bindContentCards();

    requestAnimationFrame(
      updateStickyPositions
    );

    /*
     * Critical for shared links:
     * JSON cards may not exist during the first attempt.
     * Retry once the JSON content has been inserted.
     */
    if (getSharedContentData()) {
      handleSharedContentHash();
    }
  }

  /* =========================================================
     STICKY LAYOUT
     ========================================================= */
  function updateStickyPositions() {
    if (!screenSection) return;

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
    setActiveButton(footballButton);
    setActiveNav(navFootball);
    updateFootballControls();
  }

  function openHighlights() {
    showContent("highlights");
    setActiveButton(highlightsButton);
    setActiveNav(navHighlights);
  }

  function openTV() {
    showContent("tv");
    setActiveButton(tvButton);
    setActiveNav(navTV);
  }

  function openMovies() {
    showContent("movies");
    setActiveButton(moviesButton);
    setActiveNav(navMovies);
  }

  footballButton?.addEventListener(
    "click",
    openFootball
  );

  highlightsButton?.addEventListener(
    "click",
    openHighlights
  );

  tvButton?.addEventListener(
    "click",
    openTV
  );

  moviesButton?.addEventListener(
    "click",
    openMovies
  );

  navFootball?.addEventListener(
    "click",
    function (event) {
      event.preventDefault();
      openFootball();
    }
  );

  navHighlights?.addEventListener(
    "click",
    function (event) {
      event.preventDefault();
      openHighlights();
    }
  );

  navTV?.addEventListener(
    "click",
    function (event) {
      event.preventDefault();
      openTV();
    }
  );

  navMovies?.addEventListener(
    "click",
    function (event) {
      event.preventDefault();
      openMovies();
    }
  );

  /* =========================================================
     FOOTBALL CONTROLS
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
        mainScreenButton.classList.remove(
          "active"
        );
      }

      if (altScreenButton) {
        altScreenButton.classList.remove(
          "active"
        );
      }

      return;
    }

    if (mainScreenButton) {
      mainScreenButton.classList.add(
        "active"
      );
    }

    if (altScreenButton) {
      altScreenButton.classList.remove(
        "active"
      );
    }
  }

  /* =========================================================
     ALT SCREEN
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
      mainScreenButton.classList.add(
        "active"
      );
    }

    if (altScreenButton) {
      altScreenButton.classList.remove(
        "active"
      );
    }
  }

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

      setTimeout(function () {
        if (
          currentScreenType === "match" &&
          hasValidAltUrl(currentAltUrl)
        ) {
          altScreenFrame.src =
            currentAltUrl;
        }
      }, 80);
    }

    if (altScreenOverlay) {
      altScreenOverlay.hidden =
        false;
    }

    document.body.classList.add(
      "alt-screen-open"
    );

    if (mainScreenButton) {
      mainScreenButton.classList.remove(
        "active"
      );
    }

    if (altScreenButton) {
      altScreenButton.classList.add(
        "active"
      );
    }
  }

  mainScreenButton?.addEventListener(
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

  altScreenButton?.addEventListener(
    "click",
    openAltScreen
  );

  closeAltScreen?.addEventListener(
    "click",
    hideAltScreen
  );

  altScreenOverlay?.addEventListener(
    "click",
    function (event) {
      if (
        event.target === altScreenOverlay
      ) {
        hideAltScreen();
      }
    }
  );

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
     HLS
     ========================================================= */
  function isM3U8Url(url) {
    return /\.m3u8(?:$|[?#])/i.test(
      String(url || "").trim()
    );
  }

  function destroyM3U8Player() {
    if (hlsInstance) {
      try {
        hlsInstance.destroy();
      } catch (error) {
        console.log(
          "HLS cleanup failed:",
          error
        );
      }

      hlsInstance = null;
    }

    if (hlsVideo) {
      try {
        hlsVideo.pause();
        hlsVideo.removeAttribute(
          "src"
        );
        hlsVideo.load();
        hlsVideo.remove();
      } catch (error) {
        console.log(
          "Video cleanup failed:",
          error
        );
      }

      hlsVideo = null;
    }

    if (hlsUnmuteButton) {
      hlsUnmuteButton.remove();
      hlsUnmuteButton = null;
    }
  }

  function loadHlsLibrary() {
    if (window.Hls) {
      return Promise.resolve(
        window.Hls
      );
    }

    if (hlsLibraryPromise) {
      return hlsLibraryPromise;
    }

    hlsLibraryPromise =
      new Promise(
        function (resolve, reject) {
          const existing =
            document.querySelector(
              'script[data-deeprowss-hls="true"]'
            );

          if (existing) {
            existing.addEventListener(
              "load",
              function () {
                if (window.Hls) {
                  resolve(
                    window.Hls
                  );
                } else {
                  reject(
                    new Error(
                      "HLS library loaded without Hls."
                    )
                  );
                }
              },
              { once: true }
            );

            existing.addEventListener(
              "error",
              function () {
                reject(
                  new Error(
                    "Could not load HLS library."
                  )
                );
              },
              { once: true }
            );

            return;
          }

          const script =
            document.createElement(
              "script"
            );

          script.src =
            "https://cdn.jsdelivr.net/npm/hls.js@latest/dist/hls.min.js";

          script.async = true;

          script.dataset.deeprowssHls =
            "true";

          script.onload =
            function () {
              if (window.Hls) {
                resolve(
                  window.Hls
                );
              } else {
                reject(
                  new Error(
                    "HLS library loaded without Hls."
                  )
                );
              }
            };

          script.onerror =
            function () {
              reject(
                new Error(
                  "Could not load HLS library."
                )
              );
            };

          document.head.appendChild(
            script
          );
        }
      );

    return hlsLibraryPromise;
  }

  function createM3U8Player(url) {
    if (!screenPlayer) {
      return;
    }

    destroyM3U8Player();

    if (screenFrame) {
      screenFrame.src =
        "about:blank";

      screenFrame.style.display =
        "none";
    }

    const video =
      document.createElement(
        "video"
      );

    video.className =
      "deeprowss-hls-video";

    video.setAttribute(
      "playsinline",
      ""
    );

    video.setAttribute(
      "webkit-playsinline",
      ""
    );

    video.setAttribute(
      "controls",
      ""
    );

    video.autoplay = true;
    video.muted = true;
    video.volume = 0;

    Object.assign(
      video.style,
      {
        width: "100%",
        height: "100%",
        display: "block",
        objectFit: "contain",
        background: "#000"
      }
    );

    screenPlayer.appendChild(
      video
    );

    hlsVideo = video;

    const unmuteButton =
      document.createElement(
        "button"
      );

    unmuteButton.type =
      "button";

    unmuteButton.className =
      "deeprowss-hls-unmute";

    unmuteButton.textContent =
      "🔇 Unmute";

    unmuteButton.setAttribute(
      "aria-label",
      "Unmute video"
    );

    unmuteButton.setAttribute(
      "title",
      "Click to unmute"
    );

    Object.assign(
      unmuteButton.style,
      {
        position: "absolute",
        left: "50%",
        bottom: "18px",
        transform: "translateX(-50%)",
        zIndex: "9999",
        padding: "10px 18px",
        border: "0",
        borderRadius: "999px",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "14px",
        background: "rgba(0,0,0,.85)",
        color: "#fff",
        boxShadow: "0 3px 12px rgba(0,0,0,.45)"
      }
    );

    unmuteButton.addEventListener(
      "click",
      function () {
        video.muted = false;
        video.volume = 1;

        const playResult =
          video.play();

        if (
          playResult &&
          typeof playResult.then === "function"
        ) {
          playResult.catch(
            function (error) {
              console.log(
                "Audio playback could not start:",
                error
              );
            }
          );
        }

        unmuteButton.textContent =
          "🔊 Sound On";

        setTimeout(
          function () {
            if (hlsUnmuteButton) {
              hlsUnmuteButton.style.display =
                "none";
            }
          },
          1200
        );
      }
    );

    screenPlayer.appendChild(
      unmuteButton
    );

    hlsUnmuteButton =
      unmuteButton;

    function startNativeHls() {
      video.src = url;

      video.addEventListener(
        "loadedmetadata",
        function () {
          const playResult =
            video.play();

          if (
            playResult &&
            typeof playResult.then === "function"
          ) {
            playResult.catch(
              function (error) {
                console.log(
                  "Muted autoplay was blocked:",
                  error
                );
              }
            );
          }
        },
        { once: true }
      );

      video.load();
    }

    if (
      video.canPlayType(
        "application/vnd.apple.mpegurl"
      )
    ) {
      startNativeHls();
      return;
    }

    loadHlsLibrary()
      .then(
        function (Hls) {
          if (!hlsVideo) {
            return;
          }

          if (!Hls.isSupported()) {
            console.log(
              "This browser does not support HLS."
            );
            return;
          }

          hlsInstance =
            new Hls({
              enableWorker: true
            });

          hlsInstance.loadSource(
            url
          );

          hlsInstance.attachMedia(
            video
          );

          hlsInstance.on(
            Hls.Events.MANIFEST_PARSED,
            function () {
              const playResult =
                video.play();

              if (
                playResult &&
                typeof playResult.then === "function"
              ) {
                playResult.catch(
                  function (error) {
                    console.log(
                      "Muted autoplay was blocked:",
                      error
                    );
                  }
                );
              }
            }
          );

          hlsInstance.on(
            Hls.Events.ERROR,
            function (
              event,
              data
            ) {
              if (
                data &&
                data.fatal
              ) {
                console.log(
                  "HLS playback error:",
                  data
                );
              }
            }
          );
        }
      )
      .catch(
        function (error) {
          console.log(
            "Could not start HLS player:",
            error
          );
        }
      );
  }

  /* =========================================================
     LOAD SCREEN
     ========================================================= */
  function loadScreen(
    url,
    name,
    type,
    keepMatchState
  ) {
    if (
      (!screenFrame && !screenPlayer) ||
      !url
    ) {
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

    if (
      !keepMatchState &&
      type !== "match"
    ) {
      currentMatchCard = null;
      currentMainUrl = "";
      currentAltUrl = "";
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

    /*
     * CRITICAL FIX:
     * There is only ONE main player.
     * M3U8 creates one video INSIDE screenPlayer.
     * Normal URLs use the existing screenFrame.
     * No second iframe is ever created.
     */
    if (isM3U8Url(url)) {
      createM3U8Player(url);
    } else {
      destroyM3U8Player();

      if (screenFrame) {
        screenFrame.style.display =
          "";

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

              screenLoadTimer =
                null;
            },
            150
          );
      }
    }

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

    updateFootballControls();

    requestAnimationFrame(
      updateStickyPositions
    );

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
        window.scrollBy({
          top:
            rect.top -
            headerHeight -
            10,
          behavior: "smooth"
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

    /* MATCH */
    matchCards.forEach(
      function (card) {
        if (
          card.dataset.bound ===
          "true"
        ) {
          return;
        }

        card.dataset.bound =
          "true";

        card.addEventListener(
          "click",
          function () {
            const url =
              this.dataset.url ||
              "";

            const altUrl =
              this.dataset.altUrl ||
              "";

            const name =
              this.dataset.name ||
              this.querySelector(
                ".match-teams"
              )?.textContent?.trim() ||
              "Football Match";

            currentMatchCard =
              this;

            currentMainUrl =
              url;

            currentAltUrl =
              altUrl;

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

            /*
             * IMPORTANT:
             * Open the section first, then load the URL
             * into the existing screenFrame/screenPlayer.
             */
            openFootball();

            loadScreen(
              url,
              name,
              "match",
              true
            );
          }
        );
      }
    );

    /* HIGHLIGHTS */
    highlightCards.forEach(
      function (card) {
        if (
          card.dataset.bound ===
          "true"
        ) {
          return;
        }

        card.dataset.bound =
          "true";

        card.addEventListener(
          "click",
          function () {
            const url =
              this.dataset.url ||
              "";

            const name =
              this.dataset.name ||
              this.querySelector(
                ".match-teams"
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

            currentMatchCard =
              null;

            currentMainUrl =
              "";

            currentAltUrl =
              "";

            openHighlights();

            loadScreen(
              url,
              name,
              "highlight",
              false
            );
          }
        );
      }
    );

    /* TV */
    tvChannels.forEach(
      function (channel) {
        if (
          channel.dataset.bound ===
          "true"
        ) {
          return;
        }

        channel.dataset.bound =
          "true";

        channel.addEventListener(
          "click",
          function () {
            const url =
              this.dataset.url ||
              "";

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

            currentMatchCard =
              null;

            currentMainUrl =
              "";

            currentAltUrl =
              "";

            openTV();

            loadScreen(
              url,
              name,
              "tv",
              false
            );
          }
        );
      }
    );

    /* MOVIES */
    movieCards.forEach(
      function (movie) {
        if (
          movie.dataset.bound ===
          "true"
        ) {
          return;
        }

        movie.dataset.bound =
          "true";

        movie.addEventListener(
          "click",
          function () {
            const url =
              this.dataset.url ||
              "";

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

            currentMatchCard =
              null;

            currentMainUrl =
              "";

            currentAltUrl =
              "";

            openMovies();

            loadScreen(
              url,
              name,
              "movie",
              false
            );
          }
        );
      }
    );

    /*
     * Share buttons are added only after normal
     * card click handlers exist.
     */
    addShareButtons();
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
        typeof result.then ===
          "function"
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
    } catch (error) {
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
      } catch (error) {
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
        typeof result.then ===
          "function"
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
    } catch (error) {
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
        } else {
          enterScreenFullscreen();
        }
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
            typeof lockResult.catch ===
              "function"
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
        } catch (error) {
          console.log(
            "Landscape lock unavailable:",
            error
          );
        }
      }
    } else {
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
            updateStickyPositions
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
     PAGE SAFETY
     ========================================================= */
  window.addEventListener(
    "pagehide",
    function () {
      hideAltScreen();
      destroyM3U8Player();

      if (screenFrame) {
        screenFrame.style.display =
          "";
      }

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

  /*
   * Resolve existing index.html cards immediately.
   * JSON cards are resolved again after JSON loads.
   */
  handleSharedContentHash();

  requestAnimationFrame(
    function () {
      updateStickyPositions();
    }
  );
});
