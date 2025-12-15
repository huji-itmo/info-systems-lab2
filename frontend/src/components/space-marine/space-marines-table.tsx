// components/SpaceMarinesTable.tsx
"use client";
import * as React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Loader2, Pencil, Trash, Download } from "lucide-react";
import { SpaceMarine, useDeleteSpaceMarine, useSpaceMarines } from "@/hooks/use-space-marine-hooks";
import { EditSpaceMarineDialog } from "./edit-space-marine-dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle
} from "../ui/alert-dialog";
import { AxiosError } from "axios";
import { toast } from "sonner";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useSpaceMarinesDownload } from "@/hooks/use-space-marines-download";
import { ImportSpaceMarinesDialog } from "./import-space-marines-dialog";
import { ImportControls } from "../import-controls";

interface SpaceMarinesTableProps {
  pageSize?: number;
}

export function SpaceMarinesTable({ pageSize = 10 }: SpaceMarinesTableProps) {
  const [page, setPage] = React.useState(0); // zero-based
  const [editingMarine, setEditingMarine] = React.useState<SpaceMarine | null>(null);
  const [marineToDelete, setMarineToDelete] = React.useState<SpaceMarine | null>(null);
  const [isDownloadDialogOpen, setIsDownloadDialogOpen] = React.useState(false);
  const [downloadSettings, setDownloadSettings] = React.useState({
    page: 0,
    size: 1000,
    fileType: "json",
    embed: "none",
  });

  const { data, isLoading, isError, error } = useSpaceMarines(page, pageSize);
  const deleteMutation = useDeleteSpaceMarine();
  const { downloadSpaceMarines, isDownloading } = useSpaceMarinesDownload();

  const handleEditClick = (marine: SpaceMarine) => {
    setEditingMarine(marine);
  };

  const handleDeleteClick = (marine: SpaceMarine) => {
    setMarineToDelete(marine);
  };

  const handleDialogClose = () => {
    setEditingMarine(null);
    setMarineToDelete(null);
  };

  const confirmDelete = () => {
    if (!marineToDelete) return;
    deleteMutation.mutate(marineToDelete.id, {
      onSuccess: () => {
        setMarineToDelete(null);
      },
      onError: (error: AxiosError<{ error: string }>) => {
        const errorMessage = error.response?.data?.error || "Failed to delete space marine";
        toast.error(errorMessage);
      }
    });
  };

  const handleDownloadSettingsChange = (field: string, value: string | number) => {
    setDownloadSettings(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleDownload = () => {
    downloadSpaceMarines({
      page: downloadSettings.page,
      size: downloadSettings.size,
      fileType: downloadSettings.fileType as "json" | "xml",
      embed: downloadSettings.embed as "none" | "all" | "coordinates" | "chapter",
    });
  };

  if (isError) {
    return (
      <div className="p-4 text-destructive">
        Error loading Space Marines: {(error as Error).message}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Export Controls */}
      <div className="flex justify-between">
        <ImportControls />
        <Dialog open={isDownloadDialogOpen} onOpenChange={setIsDownloadDialogOpen}>
          <DialogTrigger asChild>
            <Button variant="outline" className="gap-2">
              <Download className="w-4 h-4" />
              Export Data
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>Export Space Marines</DialogTitle>
              <div className="mt-1 text-muted-foreground text-sm">
                Configure your export settings below
              </div>
            </DialogHeader>
            <div className="gap-4 grid py-4">
              <div className="items-center gap-4 grid grid-cols-4">
                <Label htmlFor="page" className="text-right">
                  Page
                </Label>
                <Input
                  id="page"
                  type="number"
                  min="0"
                  value={downloadSettings.page}
                  onChange={(e) => handleDownloadSettingsChange("page", parseInt(e.target.value) || 0)}
                  className="col-span-3"
                />
              </div>
              <div className="items-center gap-4 grid grid-cols-4">
                <Label htmlFor="size" className="text-right">
                  Page Size
                </Label>
                <Input
                  id="size"
                  type="number"
                  min="1"
                  max="1000"
                  value={downloadSettings.size}
                  onChange={(e) => handleDownloadSettingsChange("size", parseInt(e.target.value) || 1)}
                  className="col-span-3"
                />
              </div>
              <div className="items-center gap-4 grid grid-cols-4">
                <Label htmlFor="fileType" className="text-right">
                  File Type
                </Label>
                <Select
                  value={downloadSettings.fileType}
                  onValueChange={(value) => handleDownloadSettingsChange("fileType", value)}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue placeholder="Select file type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="json">JSON</SelectItem>
                    <SelectItem value="xml">XML</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="items-center gap-4 grid grid-cols-4">
                <Label htmlFor="embed" className="text-right">
                  Embed
                </Label>
                <Select
                  value={downloadSettings.embed}
                  onValueChange={(value) => handleDownloadSettingsChange("embed", value)}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue placeholder="Select embed type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">None</SelectItem>
                    <SelectItem value="coordinates">Coordinates</SelectItem>
                    <SelectItem value="chapter">Chapter</SelectItem>
                    <SelectItem value="all">All</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button
                type="submit"
                onClick={handleDownload}
                disabled={isDownloading}
              >
                {isDownloading ? (
                  <>
                    <Loader2 className="mr-2 w-4 h-4 animate-spin" />
                    Exporting
                  </>
                ) : (
                  "Download"
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* Table */}
      <div className="border rounded-md">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Chapter</TableHead>
              <TableHead>Coordinates</TableHead>
              <TableHead>Weapon</TableHead>
              <TableHead>Health</TableHead>
              <TableHead>Loyal</TableHead>
              <TableHead>Category</TableHead>
              <TableHead className="w-[80px]">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading
              ? Array.from({ length: pageSize }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="w-6 h-4" /></TableCell>
                  <TableCell><Skeleton className="w-20 h-4" /></TableCell>
                  <TableCell><Skeleton className="w-10 h-4" /></TableCell>
                  <TableCell><Skeleton className="w-24 h-4" /></TableCell>
                  <TableCell><Skeleton className="w-8 h-4" /></TableCell>
                  <TableCell><Skeleton className="w-6 h-4" /></TableCell>
                  <TableCell><Skeleton className="w-16 h-4" /></TableCell>
                  <TableCell><Skeleton className="w-8 h-8" /></TableCell>
                </TableRow>
              ))
              : data?.content.length ? (
                data.content.map((marine) => (
                  <TableRow key={marine.id}>
                    <TableCell className="font-mono">{marine.id}</TableCell>
                    <TableCell>{marine.name}</TableCell>
                    <TableCell className="font-mono">{marine.chapterId}</TableCell>
                    <TableCell className="font-mono">{marine.coordinatesId}</TableCell>
                    <TableCell>{marine.weaponType}</TableCell>
                    <TableCell>{marine.health}</TableCell>
                    <TableCell>
                      {marine.loyal === null ? "—" : marine.loyal ? "✅" : "❌"}
                    </TableCell>
                    <TableCell>{marine.category || "—"}</TableCell>
                    <TableCell>
                      <div className="flex gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEditClick(marine)}
                          className="w-8 h-8"
                        >
                          <Pencil className="w-4 h-4" />
                          <span className="sr-only">Edit {marine.name}</span>
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDeleteClick(marine)}
                          className="hover:bg-destructive/10 w-8 h-8 text-destructive hover:text-destructive"
                        >
                          <Trash className="w-4 h-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={8} className="h-24 text-center">
                    No Space Marines found.
                  </TableCell>
                </TableRow>
              )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex justify-center mt-4">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => setPage(prev => Math.max(0, prev - 1))}
                  className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                />
              </PaginationItem>
              {[...Array(data.totalPages)].map((_, index) => (
                <PaginationItem key={index}>
                  <PaginationLink
                    onClick={() => setPage(index)}
                    isActive={page === index}
                  >
                    {index + 1}
                  </PaginationLink>
                </PaginationItem>
              ))}
              <PaginationItem>
                <PaginationNext
                  onClick={() => setPage(prev => Math.min(data.totalPages - 1, prev + 1))}
                  className={page === data.totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}

      {/* Total count footer */}
      <div className="text-muted-foreground text-sm text-center">
        {data ? (
          <>
            Showing{" "}
            <strong>
              {page * pageSize + 1}–
              {Math.min((page + 1) * pageSize, data.totalElements)}
            </strong>{" "}
            of <strong>{data.totalElements}</strong> Space Marines
          </>
        ) : isLoading ? (
          "Loading..."
        ) : null}
      </div>

      {/* Edit Dialog */}
      {editingMarine && (
        <EditSpaceMarineDialog
          marine={editingMarine}
          open={!!editingMarine}
          onOpenChange={handleDialogClose}
        />
      )}

      {/* Delete Confirmation Dialog */}
      {marineToDelete && (
        <AlertDialog
          open={!!marineToDelete}
          onOpenChange={(open) => !open && setMarineToDelete(null)}
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Confirm Deletion</AlertDialogTitle>
              <AlertDialogDescription>
                Are you sure you want to delete{" "}
                <span className="font-medium">{marineToDelete.name}</span>?
                This action cannot be undone.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                onClick={confirmDelete}
                disabled={deleteMutation.isPending}
                className="bg-destructive hover:bg-destructive/90 focus:ring-destructive"
              >
                {deleteMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 w-4 h-4 animate-spin" />
                    Deleting...
                  </>
                ) : (
                  "Delete"
                )}
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      )}
    </div>
  );
}
