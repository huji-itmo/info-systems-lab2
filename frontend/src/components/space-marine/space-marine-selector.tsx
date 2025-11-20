"use client";

import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
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
import { Plus, Loader2 } from "lucide-react";
import { components } from "@/types/api.types";
import { useSpaceMarines } from "@/hooks/use-space-marine-hooks";
import { Skeleton } from "../ui/skeleton";
import { CreateSpaceMarineDialogContent } from "./create-space-marine-dialog";

type SpaceMarine = components["schemas"]["SpaceMarine"];

interface CoordinateSelectorProps {
  onSelect: (spaceMarine: SpaceMarine) => void;
  selectedSpaceMarineId?: number;
  disabled?: boolean;
}

export function SpaceMarinesSelector({ onSelect, selectedSpaceMarineId, disabled }: CoordinateSelectorProps) {
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);

  const pageSize = 5

  const { data, isLoading, error } = useSpaceMarines(page, pageSize, {
    enabled: open, // Only fetch when dialog is open
  });

  const handleSelect = (spaceMarine: SpaceMarine) => {
    onSelect(spaceMarine);
    setOpen(false);
  };

  const handleCreateSuccess = (newSpaceMarine: SpaceMarine) => {
    setCreateDialogOpen(false);
    handleSelect(newSpaceMarine);
  };

  const spaceMarines = data?.content || [];

  return (
    <>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <Button
            variant="outline"
            className="justify-start w-full"
            disabled={disabled}
          >
            {selectedSpaceMarineId ? (
              <span>Space marine ID: {selectedSpaceMarineId}</span>
            ) : (
              <span className="text-muted-foreground">Select space marine</span>
            )}
          </Button>
        </DialogTrigger>
        <DialogContent className="flex flex-col max-w-2xl max-h-[90vh] overflow-hidden">
          <DialogHeader>
            <DialogTitle>Select Space marines</DialogTitle>
          </DialogHeader>

          <div className="flex items-center gap-2 py-4">
            <Button onClick={(e) => {
              e.stopPropagation();
              setCreateDialogOpen(true);
            }}>
              <Plus className="mr-2 w-4 h-4" /> Create New
            </Button>
          </div>

          {isLoading ? (
            <div className="flex flex-1 justify-center items-center">
              <Loader2 className="w-8 h-8 text-muted-foreground animate-spin" />
            </div>
          ) : error ? (
            <div className="flex flex-col flex-1 justify-center items-center text-destructive">
              <p>Failed to load coordinates</p>
              <Button
                variant="outline"
                size="sm"
                className="mt-2"
                onClick={(e) => {
                  e.stopPropagation();
                  window.location.reload();
                }}
              >
                Retry
              </Button>
            </div>
          ) : (
            <>
              <div className="flex-1 overflow-auto">
                <div className="border rounded-md">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>ID</TableHead>
                        <TableHead>Name</TableHead>
                        <TableHead>Chapter</TableHead>
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
                        : spaceMarines.length ? (
                          spaceMarines.map((marine) => (
                            <TableRow key={marine.id}>
                              <TableCell className="font-mono">{marine.id}</TableCell>
                              <TableCell>{marine.name}</TableCell>
                              <TableCell className="font-mono">{marine.chapterId}</TableCell>
                              <TableCell>{marine.weaponType}</TableCell>
                              <TableCell>{marine.health}</TableCell>
                              <TableCell>
                                {marine.loyal === null ? "—" : marine.loyal ? "✅" : "❌"}
                              </TableCell>
                              <TableCell>{marine.category || "—"}</TableCell>
                              <TableCell>
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleSelect(marine);
                                  }}
                                >
                                  Select
                                </Button>
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
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>


      <CreateSpaceMarineDialogContent
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onSuccess={handleCreateSuccess}
      />
    </>
  );
}
