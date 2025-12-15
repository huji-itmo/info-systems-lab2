import { useQuery, UseQueryOptions } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { apiClient } from '@/lib/apiClient';
import { PaginatedResponse } from '@/types/api';

// ===== IMPORT HISTORY TYPES =====
export type ImportStatus = 'SUCCESS' | 'PARTIAL_SUCCESS' | 'FAILURE';

export interface ImportHistory {
  id: number;
  timestamp: string;
  fileName: string;
  minioObjectName?: string | null;
  contentType?: string | null;
  totalRecords?: number | null;
  successfulCount: number;
  failedCount: number;
  status: ImportStatus;
  errorMessage?: string | null;
}

// ===== IMPORT HISTORY HOOK =====
export const useImportHistory = (
  page = 0,
  size = 20,
  sort = 'timestamp,desc',
  options: Omit<
    UseQueryOptions<PaginatedResponse<ImportHistory>, AxiosError>,
    'queryKey' | 'queryFn'
  > = {},
) => {
  return useQuery<PaginatedResponse<ImportHistory>, AxiosError>({
    queryKey: ['import-history', { page, size, sort }],
    queryFn: () =>
      apiClient
        .get('/space-marines/import-history', {
          params: { page, size, sort },
        })
        .then((res) => res.data),
    ...options,
  });
};
