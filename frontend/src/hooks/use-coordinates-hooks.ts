import { apiClient } from '@/lib/apiClient';
import { PaginatedResponse } from '@/types/api';
import { components } from '@/types/api.types';
import {
  useMutation,
  useQuery,
  useQueryClient,
  UseQueryOptions,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

type CoordinatesWithId = components['schemas']['CoordinatesWithId'];

export const useCoordinates = (
  page = 0,
  size = 20,
  options: Omit<
    UseQueryOptions<PaginatedResponse<CoordinatesWithId>, AxiosError>,
    'queryKey' | 'queryFn'
  > = {},
) => {
  return useQuery<PaginatedResponse<CoordinatesWithId>, AxiosError>({
    queryKey: ['coordinates', { page, size }],
    queryFn: () =>
      apiClient
        .get('/coordinates', {
          params: { page, size },
        })
        .then((res) => res.data),
    ...options,
  });
};

// Create new coordinates
export const useCreateCoordinates = () => {
  const queryClient = useQueryClient();

  return useMutation<CoordinatesWithId, AxiosError, { x: number; y: number }>({
    mutationFn: (coordinatesData) =>
      apiClient.post('/coordinates', coordinatesData).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coordinates'] });
    },
  });
};
