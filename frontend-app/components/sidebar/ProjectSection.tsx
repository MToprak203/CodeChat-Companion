import React, { useEffect, useState } from 'react';
import { Project } from '../../entity/Project';
import { fetchProjects } from '../../services/projectService';
import { Link } from 'react-router-dom';
import ProjectUploadModal from '../modals/ProjectUploadModal';

const ProjectSection: React.FC = () => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [showModal, setShowModal] = useState(false);

  const load = () =>
    fetchProjects()
      .then(setProjects)
      .catch((err) => console.error('Failed to load projects', err));

  useEffect(() => {
    load();
    const handler = () => {
      load();
    };
    window.addEventListener('projectDeleted', handler);
    return () => window.removeEventListener('projectDeleted', handler);
  }, []);

  return (
    <section>
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-200">Projects</h3>
        <button
          type="button"
          onClick={() => setShowModal(true)}
          className="text-blue-600 text-lg leading-none"
        >
          +
        </button>
      </div>
      <div className="space-y-1 styled-scrollbar overflow-y-auto max-h-[40vh]">
        {projects.map((p) => (
          <Link
            key={p.id}
            to={`/projects/${p.id}/chat`}
            className="block px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200 truncate"
          >
            {p.name}
          </Link>
        ))}
        {projects.length === 0 && (
          <p className="text-gray-400 dark:text-gray-500 text-sm">No projects.</p>
        )}
      </div>
      {showModal && <ProjectUploadModal onClose={() => setShowModal(false)} />}
    </section>
  );
};

export default ProjectSection;
