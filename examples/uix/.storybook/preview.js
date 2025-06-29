const preview = {};

if (typeof document !== 'undefined') {
  const script = document.createElement('script');
  script.src = 'https://cdn.tailwindcss.com';
  document.head.appendChild(script);
}

export default preview;