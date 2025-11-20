// src/components/chapter-selector.tsx
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
import { Plus, Search, Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { CreateChapterDialogContent } from "./create-chapter-dialog";
import { components } from "@/types/api.types";
import { useChapters } from "@/hooks/use-chapter-hooks";

type ChapterWithId = components["schemas"]["ChapterWithId"];

interface ChapterSelectorProps {
  onSelect: (chapter: ChapterWithId) => void;
  selectedChapterId?: number;
  disabled?: boolean;
}

export function ChapterSelector({ onSelect, selectedChapterId, disabled }: ChapterSelectorProps) {
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [createDialogOpen, setCreateDialogOpen] = useState(false);

  // Fixed: Proper query options typing with enabled state
  const { data, isLoading, error } = useChapters(page, 5, {
    enabled: open, // Only fetch when dialog is open
  });

  const handleSelect = (chapter: ChapterWithId) => {
    onSelect(chapter);
    setOpen(false);
  };

  const handleCreateSuccess = (newChapter: ChapterWithId) => {
    setCreateDialogOpen(false);
    handleSelect(newChapter);
  };

  // Filter chapters by search query
  const filteredChapters = data?.content.filter(chapter =>
    chapter.name.toLowerCase().includes(search.toLowerCase())
  ) || [];

  return (
    <>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <Button
            variant="outline"
            className="justify-start w-full"
            disabled={disabled}
          >
            {selectedChapterId ? (
              <span>Chapter ID: {selectedChapterId}</span>
            ) : (
              <span className="text-muted-foreground">Select Chapter</span>
            )}
          </Button>
        </DialogTrigger>
        <DialogContent className="flex flex-col max-w-2xl max-h-[90vh] overflow-hidden">
          <DialogHeader>
            <DialogTitle>Select Chapter</DialogTitle>
          </DialogHeader>

          <div className="flex items-center gap-2 py-4">
            <div className="relative flex-1">
              <Search className="top-2.5 left-2.5 absolute w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search chapters..."
                className="pl-8"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <Button onClick={() => setCreateDialogOpen(true)}>
              <Plus className="mr-2 w-4 h-4" /> Create New
            </Button>
          </div>

          {isLoading ? (
            <div className="flex flex-1 justify-center items-center">
              <Loader2 className="w-8 h-8 text-muted-foreground animate-spin" />
            </div>
          ) : error ? (
            <div className="flex flex-col flex-1 justify-center items-center text-destructive">
              <p>Failed to load chapters</p>
              <Button
                variant="outline"
                size="sm"
                className="mt-2"
                onClick={() => window.location.reload()}
              >
                Retry
              </Button>
            </div>
          ) : (
            <>
              <div className="flex-1 overflow-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>Name</TableHead>
                      <TableHead>Marines Count</TableHead>
                      <TableHead className="w-[100px]">Action</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredChapters.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={4} className="py-8 text-muted-foreground text-center">
                          {search ? "No matching chapters found" : "No chapters found"}
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredChapters.map((chapter) => (
                        <TableRow
                          key={chapter.id}
                          className={`cursor-pointer hover:bg-muted ${selectedChapterId === chapter.id ? "bg-accent" : ""
                            }`}
                          onClick={() => handleSelect(chapter)}
                        >
                          <TableCell className="font-medium">{chapter.id}</TableCell>
                          <TableCell>{chapter.name}</TableCell>
                          <TableCell>{chapter.marinesCount}</TableCell>
                          <TableCell>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleSelect(chapter);
                              }}
                            >
                              Select
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>

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
            </>
          )}
        </DialogContent>
      </Dialog>

      <CreateChapterDialogContent
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onSuccess={handleCreateSuccess}
      />
    </>
  );
}
