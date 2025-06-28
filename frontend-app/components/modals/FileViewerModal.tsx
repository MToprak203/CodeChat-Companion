import React, { useEffect, useState } from 'react';
import { fetchProjectFile } from '../../services/projectService';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark.css';

interface Props {
  projectId: number;
  path: string;
  onClose: () => void;
}

const FileViewerModal: React.FC<Props> = ({ projectId, path, onClose }) => {
  const [highlightedContent, setHighlightedContent] = useState('');

  const ext = path.split('.').pop() || '';
  const languageMap: Record<string, string> = {
    js: 'javascript',
    jsx: 'javascript',
    ts: 'typescript',
    tsx: 'typescript',
    py: 'python',
    rb: 'ruby',
    java: 'java',
    c: 'c',
    cpp: 'cpp',
    cs: 'csharp',
    html: 'xml',
    xml: 'xml',
    css: 'css',
    scss: 'scss',
    md: 'markdown',
    json: 'json',
    sh: 'bash',
    go: 'go',
    php: 'php',
    rs: 'rust',
    kt: 'kotlin',
    swift: 'swift',
  };
  const language = languageMap[ext] || 'plaintext';

  useEffect(() => {
    fetchProjectFile(projectId, path)
      .then(raw => {
        const result = hljs.highlight(raw, { language }).value;
        setHighlightedContent(result);
      })
      .catch(e => {
        console.error('Failed to load file', e);
        setHighlightedContent('<span style="color:red">Failed to load file</span>');
      });
  }, [projectId, path, language]);

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
      <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-lg w-11/12 max-w-2xl max-h-[80vh] flex flex-col">
        <div className="flex justify-between items-center mb-2">
          <h3 className="font-semibold break-all">{path}</h3>
          <button onClick={onClose} className="text-xl leading-none">&times;</button>
        </div>
        <pre className="flex-1 overflow-auto styled-scrollbar bg-gray-100 dark:bg-gray-900 p-2 rounded text-sm whitespace-pre">
          <code
            className={`language-${language} hljs`}
            dangerouslySetInnerHTML={{ __html: highlightedContent }}
          ></code>
        </pre>
      </div>
    </div>
  );
};

export default FileViewerModal;
