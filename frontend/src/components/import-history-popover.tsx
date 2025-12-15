import { Button } from "@/components/ui/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useImportHistory } from "@/hooks/use-import-history";
import { Loader2, History } from "lucide-react";
import { useState } from "react";

export function ImportHistoryPopover() {
  const [open, setOpen] = useState(false);
  const { data, isLoading, error } = useImportHistory(0, 5, 'timestamp,desc');

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" size="icon">
          <History className="w-4 h-4" />
          <span className="sr-only">View import history</span>
        </Button>
      </PopoverTrigger>
      <PopoverContent className="p-0 w-96" align="end">
        <div className="p-4 border-border border-b">
          <h4 className="font-medium leading-none">Import History</h4>
          <p className="mt-1 text-muted-foreground text-sm">
            Recent file imports for Space Marines
          </p>
        </div>
        <div className="p-4">
          {isLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="w-6 h-6 text-muted-foreground animate-spin" />
            </div>
          ) : error ? (
            <div className="py-8 text-destructive text-sm text-center">
              Failed to load import history
            </div>
          ) : data?.content?.length === 0 ? (
            <div className="py-8 text-muted-foreground text-sm text-center">
              No import history available
            </div>
          ) : (
            <div className="max-h-[400px] overflow-y-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[30%]">File</TableHead>
                    <TableHead className="w-[20%] text-center">Status</TableHead>
                    <TableHead className="w-[25%] text-right">Success</TableHead>
                    <TableHead className="w-[25%] text-right">Failed</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data?.content?.map((importRecord) => (
                    <TableRow
                      key={importRecord.id}
                      className="hover:bg-muted/50 cursor-pointer"
                      onClick={() => setOpen(false)}
                    >
                      <TableCell className="font-medium truncate" title={importRecord.fileName}>
                        {importRecord.fileName}
                      </TableCell>
                      <TableCell className="text-center">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${importRecord.status === 'COMPLETED'
                          ? 'bg-green-100 text-green-800'
                          : importRecord.status === 'PARTIAL_SUCCESS'
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-red-100 text-red-800'
                          }`}>
                          {importRecord.status.replace('_', ' ')}
                        </span>
                      </TableCell>
                      <TableCell className="font-medium text-right">
                        {importRecord.successful}
                      </TableCell>
                      <TableCell className="font-medium text-right">
                        {importRecord.failed}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}

          {data && data.totalElements > 5 && (
            <div className="mt-3 pt-3 border-border border-t text-muted-foreground text-xs text-center">
              Showing {data.content.length} of {data.totalElements} imports
            </div>
          )}
        </div>
        <div className="flex justify-end bg-muted/30 p-3 border-border border-t">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setOpen(false)}
            className="text-xs"
          >
            Close
          </Button>
        </div>
      </PopoverContent>
    </Popover>
  );
}
