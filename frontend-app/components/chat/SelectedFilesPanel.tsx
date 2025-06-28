import React from 'react';

interface Props {
  files: string[];
}

const SelectedFilesPanel: React.FC<Props> = ({ files }) => {
  if (files.length === 0) return null;
  return (
    <div className="p-2 border-b bg-gray-50 dark:bg-gray-800 text-xs shadow-inner">
      <h4 className="font-semibold mb-1">Selected Files</h4>
      <ul className="list-disc pl-4 max-h-32 overflow-y-auto styled-scrollbar space-y-1">
        {files.map((f) => (
          <li key={f} className="break-all">
            {f}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default SelectedFilesPanel;
