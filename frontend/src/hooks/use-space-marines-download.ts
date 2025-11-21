// hooks/use-space-marines-download.ts
import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { apiClient } from '@/lib/apiClient';
import { toast } from 'sonner';

export interface DownloadParams {
  page?: number;
  size?: number;
  embed?: 'none' | 'all' | 'coordinates' | 'chapter';
  fileType: 'json' | 'xml';
}

export function useSpaceMarinesDownload() {
  const [isDownloading, setIsDownloading] = useState(false);

  const downloadMutation = useMutation<
    Blob,
    AxiosError<{ error: string }>,
    DownloadParams
  >({
    mutationFn: async ({ page = 0, size = 1000, embed = 'none', fileType }) => {
      setIsDownloading(true);

      // Build query parameters
      const params: Record<string, string | number> = {
        page,
        size,
      };

      // Only add embed if not "none"
      if (embed !== 'none') {
        params.embed = embed;
      }

      // Determine endpoint based on file type
      const endpoint = `/space-marines/export/${fileType}`;

      // Make request with responseType blob for file download
      const response = await apiClient.get(endpoint, {
        params,
        responseType: 'blob', // Important for file download
      });

      return response.data;
    },
    onSuccess: (data, variables) => {
      // Create blob and download link
      const blob = new Blob([data], {
        type:
          variables.fileType === 'json'
            ? 'application/json'
            : 'application/xml',
      });

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      // Generate filename with parameters
      const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
      link.download = `space_marines_page_${variables.page}_size_${variables.size}_${timestamp}.${variables.fileType}`;

      document.body.appendChild(link);
      link.click();

      // Clean up
      window.URL.revokeObjectURL(url);
      document.body.removeChild(link);

      toast.success('Export downloaded successfully');
    },
    onError: (error) => {
      console.error('Download error:', error);
      const errorMessage =
        error.response?.data?.error || 'Failed to download export';
      toast.error(errorMessage);
    },
    onSettled: () => {
      setIsDownloading(false);
    },
  });

  const downloadSpaceMarines = (params: DownloadParams) => {
    downloadMutation.mutate(params);
  };

  return {
    downloadSpaceMarines,
    isDownloading: isDownloading || downloadMutation.isPending,
    error: downloadMutation.error,
  };
}
