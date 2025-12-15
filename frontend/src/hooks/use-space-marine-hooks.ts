import {
  useMutation,
  useQuery,
  useQueryClient,
  UseQueryOptions,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { apiClient } from '@/lib/apiClient';
import { PaginatedResponse } from '@/types/api';

// ===== TYPE DEFINITIONS =====
export type WeaponType =
  | 'BOLTGUN'
  | 'HEAVY_BOLTGUN'
  | 'FLAMER'
  | 'HEAVY_FLAMER'
  | 'MULTI_MELTA';
export type MarineCategory =
  | 'AGGRESSOR'
  | 'INCEPTOR'
  | 'TACTICAL'
  | 'CHAPLAIN'
  | 'APOTHECARY';

export interface SpaceMarine {
  id: number;
  name: string;
  coordinatesId: number;
  creationDate: string;
  chapterId: number;
  health: number;
  loyal?: boolean | null;
  category?: MarineCategory | null;
  weaponType: WeaponType;
}

export interface Coordinates {
  id?: number;
  x: number;
  y: number;
}

export interface Chapter {
  id?: number;
  name: string;
  marinesCount: number;
}

// ===== SPECIAL OPERATIONS HOOKS =====
export const useHealthSum = () =>
  useQuery<number, AxiosError>({
    queryKey: ['health', 'sum'],
    queryFn: () =>
      apiClient.get('/special-operations/health/sum').then((res) => res.data),
  });

export const useHealthAverage = () =>
  useQuery<number, AxiosError>({
    queryKey: ['health', 'average'],
    queryFn: () =>
      apiClient
        .get('/special-operations/health/average')
        .then((res) => res.data),
  });

export const useFilteredSpaceMarines = (
  weaponTypes: WeaponType[],
  page = 0,
  size = 20,
  options?: UseQueryOptions<PaginatedResponse<SpaceMarine>, AxiosError>,
) =>
  useQuery<PaginatedResponse<SpaceMarine>, AxiosError>({
    queryKey: ['space-marines', 'filtered', { weaponTypes, page, size }],
    queryFn: () =>
      apiClient
        .get('/special-operations/space-marines/filter-by-weapons', {
          params: { weaponTypes, page, size },
        })
        .then((res) => res.data),
    enabled: weaponTypes.length > 0,
    ...options,
  });

export const useAssignMarineToChapter = () => {
  const queryClient = useQueryClient();

  return useMutation<
    SpaceMarine,
    AxiosError<{ error: string }>,
    { chapterId: number; marineId: number }
  >({
    mutationFn: ({ chapterId, marineId }) =>
      apiClient
        .put(
          `/special-operations/chapters/${chapterId}/assign-marine/${marineId}`,
        )
        .then((res) => res.data),
    onSuccess: (_, { marineId }) => {
      queryClient.invalidateQueries({ queryKey: ['space-marine', marineId] });
      queryClient.invalidateQueries({ queryKey: ['space-marines'] });
      queryClient.invalidateQueries({ queryKey: ['chapters'] });
    },
  });
};

// ===== SPACE MARINES HOOKS =====
export const useSpaceMarines = (
  page = 0,
  size = 20,
  options: Omit<
    UseQueryOptions<PaginatedResponse<SpaceMarine>, AxiosError>,
    'queryKey' | 'queryFn'
  > = {},
) =>
  useQuery<PaginatedResponse<SpaceMarine>, AxiosError>({
    queryKey: ['space-marines', { page, size }],
    queryFn: () =>
      apiClient
        .get('/space-marines', { params: { page, size } })
        .then((res) => res.data),
    ...options,
  });

export const useSpaceMarine = (
  id: number,
  options?: UseQueryOptions<SpaceMarine, AxiosError>,
) =>
  useQuery<SpaceMarine, AxiosError>({
    queryKey: ['space-marine', id],
    queryFn: () =>
      apiClient.get(`/space-marines/${id}`).then((res) => res.data),
    enabled: !!id,
    ...options,
  });

export const useCreateSpaceMarine = () => {
  const queryClient = useQueryClient();

  return useMutation<
    SpaceMarine,
    AxiosError,
    Omit<SpaceMarine, 'id' | 'creationDate'>
  >({
    mutationFn: (marine) =>
      apiClient.post('/space-marines', marine).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['space-marines'] });
      queryClient.invalidateQueries({ queryKey: ['health'] });
    },
  });
};

export const useUpdateSpaceMarine = () => {
  const queryClient = useQueryClient();

  return useMutation<
    SpaceMarine,
    AxiosError,
    Partial<Omit<SpaceMarine, 'creationDate'>> & { id: number }
  >({
    mutationFn: ({ id, ...data }) =>
      apiClient.put(`/space-marines/${id}`, data).then((res) => res.data),
    onSuccess: (marine) => {
      queryClient.invalidateQueries({ queryKey: ['space-marine', marine.id] });
      queryClient.invalidateQueries({ queryKey: ['space-marines'] });
      queryClient.invalidateQueries({ queryKey: ['health'] });
    },
  });
};

export const useDeleteSpaceMarine = () => {
  const queryClient = useQueryClient();

  return useMutation<void, AxiosError<{ error: string }>, number>({
    mutationFn: (id) => apiClient.delete(`/space-marines/${id}`),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['space-marines'] });
      queryClient.invalidateQueries({
        queryKey: ['space-marine', id],
        exact: true,
      });
      queryClient.invalidateQueries({ queryKey: ['health'] });
    },
  });
};
