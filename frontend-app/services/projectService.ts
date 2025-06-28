import { postFormData, get, post, del } from './httpClient';
import { API_BASE_URL } from '../config';
import { Project } from '../entity/Project';
import { Conversation } from '../entity/Conversation';

const IGNORED_DIRS = [
  'node_modules',
  'target',
  'build',
  'dist',
  'out',
  '.git',
  '.gradle',
  '.idea',
  'venv',
  '.venv',
  ".mvn",
  "mvn"
];

const ALLOWED_FILENAMES = [
  'pom.xml',
  'package.json',
  'package-lock.json',
  'tsconfig.json',
  'vite.config.ts',
  'tailwind.config.js',
  'postcss.config.js',
  'build.gradle',
  'settings.gradle',
  'gradle.properties',
  '.gitignore',
  'README.md',
  'readme.md'
];

const ALLOWED_EXTENSIONS = [
  '.java',
  '.kt',
  '.ts',
  '.tsx',
  '.js',
  '.jsx',
  '.json',
  '.xml',
  '.yml',
  '.yaml',
  '.gradle',
  '.properties',
  '.md',
  '.html',
  '.css',
  '.scss',
  '.txt',
  ".sql"
];

function isIgnored(path: string): boolean {
  return path
    .split('/')
    .some((part) => IGNORED_DIRS.includes(part));
}

function isAllowed(path: string): boolean {
  const name = path.split('/').pop() || '';
  if (ALLOWED_FILENAMES.includes(name)) return true;
  const idx = name.lastIndexOf('.');
  if (idx === -1) return false;
  const ext = name.slice(idx);
  return ALLOWED_EXTENSIONS.includes(ext);
}

export function filterProjectFiles(files: FileList): File[] {
  return Array.from(files).filter((f: any) => {
    const path = f.webkitRelativePath || f.name;
    return !isIgnored(path) && isAllowed(path);
  });
}

export async function uploadProject(name: string, files: FileList): Promise<Project> {
  const form = new FormData();
  form.append('name', name);
  filterProjectFiles(files).forEach((f: any) => {
    const path = f.webkitRelativePath || f.name;
    form.append('files', f, path);
  });

  const res = await postFormData<FormData, Project>(`${API_BASE_URL}/projects`, form);
  if (!res.success || !res.data) {
    throw new Error(res.error?.message || 'Failed to upload project');
  }
  return res.data;
}

export async function syncProject(id: number, files: FileList, name?: string): Promise<void> {
  const form = new FormData();
  if (name) form.append('name', name);
  filterProjectFiles(files).forEach((f: any) => {
    const path = f.webkitRelativePath || f.name;
    form.append('files', f, path);
  });

  const res = await postFormData<FormData, void>(`${API_BASE_URL}/projects/${id}/sync`, form);
  if (!res.success) {
    throw new Error(res.error?.message || 'Failed to sync project');
  }
}

export async function fetchProjectTree(id: number): Promise<string[]> {
  const res = await get<string[]>(`${API_BASE_URL}/projects/${id}/tree`);
  if (!res.success || !res.data) {
    throw new Error(res.error?.message || 'Failed to fetch project tree');
  }
  return res.data;
}

export async function createProjectConversation(id: number) {
  const res = await post<void, Conversation>(
    `${API_BASE_URL}/projects/${id}/conversation`
  );
  if (!res.success || !res.data) {
    throw new Error(
      res.error?.message || 'Failed to create project conversation'
    );
  }
  return res.data;
}

export async function fetchProjectConversations(id: number): Promise<Conversation[]> {
  const res = await get<Conversation[]>(`${API_BASE_URL}/projects/${id}/conversation`);
  if (!res.success || !res.data) return [];
  return res.data;
}

export async function sendSelectedFiles(
  id: number,
  files: string[]
): Promise<void> {
  // Placeholder API call
  await post<string[], void>(
    `${API_BASE_URL}/projects/${id}/selected-files`,
    files
  ).catch(() => {});
}

export async function fetchSelectedFiles(id: number): Promise<string[]> {
  const res = await get<string[]>(`${API_BASE_URL}/projects/${id}/selected-files`);
  if (!res.success || !res.data) return [];
  return res.data;
}

export async function fetchProjectFile(
  id: number,
  path: string
): Promise<string> {
  const res = await get<string>(
    `${API_BASE_URL}/projects/${id}/file?path=${encodeURIComponent(path)}`
  );
  if (!res.success || !res.data) {
    throw new Error(res.error?.message || 'Failed to fetch file');
  }
  return res.data;
}

export async function fetchProjects(): Promise<Project[]> {
  const res = await get<Project[]>(`${API_BASE_URL}/projects`);
  if (!res.success || !res.data) {
    throw new Error(res.error?.message || 'Failed to fetch projects');
  }
  return res.data;
}

export async function fetchProject(id: number): Promise<Project | null> {
  const projects = await fetchProjects();
  return projects.find((p) => p.id === id) || null;
}

export async function leaveProject(id: number): Promise<void> {
  const res = await del<void>(`${API_BASE_URL}/projects/${id}`);
  if (!res.success) {
    throw new Error(res.error?.message || 'Failed to leave project');
  }
}
