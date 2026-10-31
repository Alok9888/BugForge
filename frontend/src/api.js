const USER_KEY = 'reactcode.userId';

function userId() {
  let id = localStorage.getItem(USER_KEY);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(USER_KEY, id);
  }
  return id;
}

export async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId(),
      ...options.headers,
    },
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message || `Request failed (${response.status})`);
  }
  return response.json();
}

export const draftKey = (slug) => `reactcode.draft.${slug}`;
export const completionKey = 'reactcode.completed';

export function getCompleted() {
  try {
    return new Set(JSON.parse(localStorage.getItem(completionKey) || '[]'));
  } catch {
    return new Set();
  }
}

export function complete(slug) {
  const completed = getCompleted();
  completed.add(slug);
  localStorage.setItem(completionKey, JSON.stringify([...completed]));
}
