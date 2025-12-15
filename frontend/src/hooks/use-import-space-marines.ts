// hooks/use-import-space-marines.ts
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { apiClient } from '@/lib/apiClient';
import { toast } from 'sonner';

// Interfaces matching the API schema
export interface ImportFailure {
  name: string;
  reason: string;
}

export interface ImportSummary {
  total: number;
  successful: number;
  failed: ImportFailure[];
}

export function useImportSpaceMarines() {
  const queryClient = useQueryClient();

  return useMutation<ImportSummary, AxiosError<{ error: string }>, File>({
    mutationFn: async (file) => {
      const formData = new FormData();
      formData.append('file', file);

      const response = await apiClient.post<ImportSummary>(
        '/space-marines/import',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        },
      );

      return response.data;
    },
    onSuccess: (data) => {
      // Invalidate relevant queries
      queryClient.invalidateQueries({ queryKey: ['space-marines'] });
      queryClient.invalidateQueries({ queryKey: ['health'] });
      queryClient.invalidateQueries({ queryKey: ['import-history'] });
      // Show success toast with summary
      toast.success(
        `Import completed: ${data.successful} successful, ${data.failed.length} failed`,
      );
    },
    onError: (error) => {
      const errorMessage =
        error.response?.data?.error || 'Failed to import Space Marines';
      toast.error(errorMessage);
    },
  });
}
