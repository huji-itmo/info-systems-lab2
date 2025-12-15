import {
  useMutation,
  useQuery,
  useQueryClient,
  UseQueryOptions,
} from '@tanstack/react-query';
import { apiClient } from '@/lib/apiClient';
import { AxiosError } from 'axios';
import { components } from '@/types/api.types';
import { PaginatedResponse } from '@/types/api';

type ChapterWithId = components['schemas']['ChapterWithId'];

// Get all chapters with pagination
export const useChapters = (
  page = 0,
  size = 20,
  options: Omit<
    UseQueryOptions<PaginatedResponse<ChapterWithId>, AxiosError>,
    'queryKey' | 'queryFn'
  > = {},
) => {
  return useQuery<PaginatedResponse<ChapterWithId>, AxiosError>({
    queryKey: ['chapters', { page, size }],
    queryFn: () =>
      apiClient
        .get('/chapters', {
          params: { page, size },
        })
        .then((res) => res.data),
    ...options,
  });
};
// Create a new chapter
export const useCreateChapter = () => {
  const queryClient = useQueryClient();

  return useMutation<
    ChapterWithId,
    AxiosError,
    { name: string; marinesCount: number }
  >({
    mutationFn: (chapterData) =>
      apiClient.post('/chapters', chapterData).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['chapters'] });
    },
  });
};
