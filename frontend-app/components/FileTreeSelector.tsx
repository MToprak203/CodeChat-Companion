import React, { useState } from 'react';

export interface FileNode {
  name: string;
  path: string;
  children?: FileNode[];
  type: 'file' | 'dir';
}

interface FileTreeSelectorProps {
  tree: FileNode[];
  selected: Set<string>;
  onToggle: (file: string) => void;
  onClear?: () => void;
  onOpen?: (file: string) => void;
}

export function buildFileTree(paths: string[]): FileNode[] {
  type NodeMap = Record<string, { node: FileNode; children: NodeMap }>;
  const root: NodeMap = {};

  for (const fullPath of paths) {
    const parts = fullPath.split('/');
    let current = root;
    let currPath = '';
    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      currPath = currPath ? `${currPath}/${part}` : part;
      if (!current[part]) {
        current[part] = {
          node: {
            name: part,
            path: currPath,
            type: i === parts.length - 1 ? 'file' : 'dir',
          },
          children: {},
        };
      }
      current = current[part].children;
    }
  }

  const mapToArray = (m: NodeMap): FileNode[] =>
    Object.values(m)
      .sort((a, b) => a.node.name.localeCompare(b.node.name))
      .map(({ node, children }) => {
        if (node.type === 'dir') {
          return { ...node, children: mapToArray(children) };
        }
        return node;
      });

  return mapToArray(root);
}


interface DirectoryNodeProps {
  node: FileNode;
  renderNode: (node: FileNode) => React.ReactNode;
}

const DirectoryNode: React.FC<DirectoryNodeProps> = ({ node, renderNode }) => {
  const [open, setOpen] = useState(false);
  return (
    <li className="ml-2">
      <div
        className="cursor-pointer select-none whitespace-nowrap flex items-center hover:bg-gray-100 dark:hover:bg-gray-700 rounded px-1"
        onClick={() => setOpen((v) => !v)}
      >
        <span className="mr-1 w-3 inline-block">{open ? '▾' : '▸'}</span>
        <span className="mr-1">📁</span>
        {node.name}
      </div>
      {open && node.children && (
        <ul className="ml-4 space-y-0.5">{node.children.map(renderNode)}</ul>
      )}
    </li>
  );
};

const FileTreeSelector: React.FC<FileTreeSelectorProps> = ({
  tree,
  selected,
  onToggle,
  onClear,
  onOpen,
}) => {
  const renderNode = (node: FileNode) => {
    if (node.type === 'dir') {
      return <DirectoryNode key={node.path} node={node} renderNode={renderNode} />;
    }
    return (
      <li
        key={node.path}
        className="flex items-center whitespace-nowrap pl-5 relative hover:bg-gray-100 dark:hover:bg-gray-700 rounded px-1"
      >
        <input
          type="checkbox"
          className="h-4 w-4 text-blue-600 transition-all duration-150 border-gray-300 rounded focus:ring-2 focus:ring-blue-400 mr-2 flex-shrink-0"
          checked={selected.has(node.path)}
          onChange={() => onToggle(node.path)}
        />
        <span className="mr-1">📄</span>
        <span
          className="cursor-pointer hover:underline break-all"
          onClick={() => onOpen && onOpen(node.path)}
        >
          {node.name}
        </span>
      </li>
    );
  };

  return (
    <div className="styled-scrollbar overflow-y-auto">
      <ul className="text-sm space-y-0.5">{tree.map(renderNode)}</ul>
    </div>
  );
};

export default FileTreeSelector;
