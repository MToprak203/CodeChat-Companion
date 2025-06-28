export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL as string;
export const WS_BASE_URL = API_BASE_URL.replace(/^http/, 'ws').replace(/\/api\/v\d+$/, '');
export const AI_USER_ID = -1;
export const AI_USER_NAME = 'AI Assistant';
