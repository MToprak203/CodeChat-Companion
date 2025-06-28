import React, { useState } from 'react';
import { uploadProject, filterProjectFiles } from '../../services/projectService';
import { useNavigate } from 'react-router-dom';
import { Input } from '../Input';
import { Button } from '../Button';

interface Props {
  onClose: () => void;
}

const ProjectUploadModal: React.FC<Props> = ({ onClose }) => {
  const [name, setName] = useState('');
  const [files, setFiles] = useState<FileList | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleUpload = async () => {
    if (!name.trim() || !files) return;
    setLoading(true);
    try {
      const project = await uploadProject(name, files);
      localStorage.setItem('uploading_project', String(project.id));
      onClose();
      navigate(`/projects/${project.id}/chat`);
    } catch (err) {
      console.error(err);
      alert('Upload failed');
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
      <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-lg space-y-4 w-96">
        <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100">New Project</h2>
        <Input value={name} onChange={setName} placeholder="Project name" />
        <input
          type="file"
          webkitdirectory="true"
          multiple
          onChange={(e) => {
            if (!e.target.files) return;
            const filtered = filterProjectFiles(e.target.files);
            const dt = new DataTransfer();
            filtered.forEach((f) => dt.items.add(f));
            setFiles(dt.files);
          }}
        />
        <div className="flex justify-end space-x-2">
          <Button onClick={onClose} className="bg-gray-500 hover:bg-gray-600">Cancel</Button>
          <Button onClick={handleUpload} className="bg-blue-600 hover:bg-blue-700">
            {loading ? 'Uploading...' : 'Upload'}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ProjectUploadModal;
