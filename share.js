(function () {
  const TYPES = [
    { selector: '.match-card',     type: 'football',   navBtn: '#footballButton'   },
    { selector: '.highlight-card', type: 'highlights', navBtn: '#highlightsButton' },
    { selector: '.tv-channel',     type: 'tv',          navBtn: '#tvButton'         },
    { selector: '.movie-card',     type: 'movies',      navBtn: '#moviesButton'     }
  ];

  function slugify(str) {
    return String(str).toLowerCase().trim()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '');
  }

  function addShareButtons() {
    TYPES.forEach(({ selector, type }) => {
      document.querySelectorAll(selector).forEach(card => {
        if (card.querySelector('.share-btn')) return;

        const name = card.dataset.name || card.textContent.trim();
        card.dataset.shareSlug = slugify(name);

        const btn = document.createElement('span');
        btn.className = 'share-btn';
        btn.setAttribute('role', 'button');
        btn.setAttribute('tabindex', '0');
        btn.setAttribute('aria-label', 'Share ' + name);
        btn.textContent = '🔗';

        btn.addEventListener('click', e => {
          e.stopPropagation();
          e.preventDefault();
          shareCard(type, card.dataset.shareSlug, name);
        });
        btn.addEventListener('keydown', e => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            btn.click();
          }
        });

        card.appendChild(btn);
      });
    });
  }

  function shareCard(type, slug, name) {
    const url = `${location.origin}${location.pathname}#${type}=${slug}`;

    if (navigator.share) {
      navigator.share({ title: name, url }).catch(() => {});
    } else if (navigator.clipboard) {
      navigator.clipboard.writeText(url).then(() => toast('Link copied!'))
        .catch(() => prompt('Copy this link:', url));
    } else {
      prompt('Copy this link:', url);
    }
  }

  function toast(msg) {
    const t = document.createElement('div');
    t.className = 'share-toast';
    t.textContent = msg;
    document.body.appendChild(t);
    requestAnimationFrame(() => t.classList.add('show'));
    setTimeout(() => {
      t.classList.remove('show');
      t.addEventListener('transitionend', () => t.remove());
    }, 1600);
  }

  function openFromHash() {
    const hash = location.hash.slice(1);
    if (!hash || !hash.includes('=')) return;
    const [type, slug] = hash.split('=');
    const entry = TYPES.find(t => t.type === type);
    if (!entry) return;

    const navBtn = document.querySelector(entry.navBtn);
    if (navBtn) navBtn.click();

    setTimeout(() => {
      const card = document.querySelector(`${entry.selector}[data-share-slug="${slug}"]`);
      if (card) {
        card.click();
        card.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }, 50);
  }

  document.addEventListener('DOMContentLoaded', () => {
    addShareButtons();
    openFromHash();
  });
})();
