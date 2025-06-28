import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import ChatView from '../components/chat/ChatView';
import ChatPlaceholder from '../components/ChatPlaceholder';
import FileTreeSelector, { buildFileTree, FileNode } from '../components/FileTreeSelector';
import { Conversation } from '../entity/Conversation';
import { fetchProjectTree, fetchProject, leaveProject } from '../services/projectService';
import { createWebSocket } from '../services/wsClient';
import ProjectConversationSection from '../components/sidebar/ProjectConversationSection';
import { logout } from '../services/authService';
import FileViewerModal from '../components/modals/FileViewerModal';
import ProjectUpdateModal from '../components/modals/ProjectUpdateModal';

const ProjectChatPage: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const [conversation, setConversation] = useState<Conversation | null>(null);
  const [tree, setTree] = useState<FileNode[]>([]);
  const [treeLoading, setTreeLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [sidebarWidth, setSidebarWidth] = useState(288); // 18rem
  const [selectedFiles, setSelectedFiles] = useState<Set<string>>(new Set());
  const [viewFile, setViewFile] = useState<string | null>(null);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [projectName, setProjectName] = useState('');

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      navigate('/login');
    }
  };

  useEffect(() => {
    if (!projectId) return;
    const id = parseInt(projectId, 10);
    const uploadingId = localStorage.getItem('uploading_project');
    if (uploadingId && parseInt(uploadingId, 10) === id) {
      setUploading(true);
    }
    setTreeLoading(true);
    fetchProject(id)
      .then((p) => p && setProjectName(p.name))
      .catch((e) => console.error('Failed to fetch project', e));
    fetchProjectTree(id)
      .then((paths) => setTree(buildFileTree(paths)))
      .catch((e) => console.error('Failed to fetch tree', e))
      .finally(() => setTreeLoading(false));
  }, [projectId]);

  const toggleFile = (file: string) => {
    setSelectedFiles((prev) => {
      const next = new Set(prev);
      if (next.has(file)) {
        next.delete(file);
      } else {
        next.add(file);
      }
      return next;
    });
  };

  const clearSelection = () => {
    setSelectedFiles(new Set());
  };

  const startResize = (e: React.MouseEvent<HTMLDivElement>) => {
    const startX = e.clientX;
    const startWidth = sidebarWidth;
    const onMove = (ev: MouseEvent) => {
      const newWidth = Math.min(
        1000,
        Math.max(200, startWidth + ev.clientX - startX)
      );
      setSidebarWidth(newWidth);
    };
    const onUp = () => {
      window.removeEventListener('mousemove', onMove);
      window.removeEventListener('mouseup', onUp);
    };
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
  };

  useEffect(() => {
    if (!uploading || !projectId) return;
    const userId = localStorage.getItem('user_id');
    if (!userId) return;
    const ws = createWebSocket(`/ws/${userId}/notifications`);
    ws.onmessage = (e) => {
      if (e.data === 'Project upload completed') {
        localStorage.removeItem('uploading_project');
        setUploading(false);
        const id = parseInt(projectId, 10);
        setTreeLoading(true);
        fetchProjectTree(id)
          .then((paths) => setTree(buildFileTree(paths)))
          .catch((err) => console.error('Failed to fetch tree', err))
          .finally(() => setTreeLoading(false));
      }
    };
    ws.onerror = () => ws.close();
    return () => ws.close();
  }, [uploading, projectId]);

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-gray-100">
      <div
        className="border-r dark:border-gray-700 p-4 overflow-y-auto styled-scrollbar flex flex-col space-y-4"
        style={{ width: sidebarWidth }}
      >
        <Link to="/chat" className="text-blue-600 block mb-2">
          &larr; Back
        </Link>
        {projectName && (
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-lg font-semibold text-gray-700 dark:text-gray-200">
              {projectName}
            </h2>
            <button
              onClick={async () => {
                if (!window.confirm('Leave this project?')) return;
                try {
                  await leaveProject(parseInt(projectId!, 10));
                  window.dispatchEvent(new CustomEvent('projectDeleted', { detail: projectId }));
                  navigate('/chat');
                } catch (err) {
                  console.error('Failed to leave project', err);
                }
              }}
              className="text-red-600 text-sm hover:text-red-800"
            >
              Leave
            </button>
          </div>
        )}
          <div className="flex flex-col">
          <div className="flex items-center justify-between mb-2">
            <h3 className="font-semibold">Project Files</h3>
            <div className="space-x-2">
              <button
                onClick={() => setShowUpdateModal(true)}
                className="text-xs bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 font-medium px-2 py-1 rounded hover:bg-gray-300 dark:hover:bg-gray-600 transition"
              >
                Update
              </button>
              {selectedFiles.size > 0 && (
                <button
                  onClick={clearSelection}
                  className="text-xs bg-blue-100 dark:bg-blue-900 text-blue-700 dark:text-blue-200 font-medium px-2 py-1 rounded hover:bg-blue-200 dark:hover:bg-blue-800 transition"
                >
                  Clear Selection
                </button>
              )}
            </div>
          </div>
          <div className="styled-scrollbar overflow-y-auto max-h-[40vh]">
            {treeLoading || uploading ? (
              <p className="text-center text-xs text-gray-400 dark:text-gray-500 py-2">
                {uploading ? 'Uploading...' : 'Loading...'}
              </p>
            ) : (
              <FileTreeSelector
                tree={tree}
                selected={selectedFiles}
                onToggle={toggleFile}
                onOpen={(f) => setViewFile(f)}
              />
            )}
          </div>
        </div>
        <ProjectConversationSection
          projectId={parseInt(projectId!, 10)}
          onSelect={(c) => setConversation(c)}
        />
        <button
          onClick={handleLogout}
          className="mt-auto px-4 py-2 rounded-lg text-sm font-semibold bg-red-600 text-white hover:bg-red-700 transition-colors shadow-md dark:shadow-none"
        >
          Logout
        </button>
      </div>
      <div
        onMouseDown={startResize}
        className="w-1 cursor-col-resize bg-gray-300 dark:bg-gray-700"
      />
      <div className="flex-1 flex p-4 overflow-hidden">
        {conversation ? (
          <ChatView
            conversation={conversation}
            projectId={parseInt(projectId!, 10)}
            selectedFiles={selectedFiles}
            onUpdateTitle={(t) =>
              setConversation({ ...conversation, title: t })
            }
            onDeleted={() => setConversation(null)}
          />
        ) : (
          <div className="flex-1 flex items-center justify-center">
            <ChatPlaceholder />
          </div>
        )}
      </div>
      {viewFile && (
        <FileViewerModal
          projectId={parseInt(projectId!, 10)}
          path={viewFile}
          onClose={() => setViewFile(null)}
        />
      )}
      {showUpdateModal && (
        <ProjectUpdateModal
          projectId={parseInt(projectId!, 10)}
          onClose={() => setShowUpdateModal(false)}
          onStart={() => setUploading(true)}
        />
      )}
    </div>
  );
};

export default ProjectChatPage;
