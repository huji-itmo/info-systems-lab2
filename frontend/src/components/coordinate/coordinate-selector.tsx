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
import { components } from "@/types/api.types";
import { useCoordinates } from "@/hooks/use-coordinates-hooks";
import { CreateCoordinateDialogContent } from "./create-coordinate-dialog";

type CoordinatesWithId = components["schemas"]["CoordinatesWithId"];

interface CoordinateSelectorProps {
  onSelect: (coordinate: CoordinatesWithId) => void;
  selectedCoordinateId?: number;
  disabled?: boolean;
}

export function CoordinateSelector({ onSelect, selectedCoordinateId, disabled }: CoordinateSelectorProps) {
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [createDialogOpen, setCreateDialogOpen] = useState(false);

  const { data, isLoading, error } = useCoordinates(page, 5, {
    enabled: open, // Only fetch when dialog is open
  });

  const handleSelect = (coordinate: CoordinatesWithId) => {
    onSelect(coordinate);
    setOpen(false);
  };

  const handleCreateSuccess = (newCoordinate: CoordinatesWithId) => {
    setCreateDialogOpen(false);
    handleSelect(newCoordinate);
  };

  // Filter coordinates by search query (search by ID or coordinates)
  const filteredCoordinates = data?.content?.filter(coordinate => {
    const searchTerm = search.toLowerCase();
    return (
      coordinate.id?.toString().includes(searchTerm) ||
      coordinate.x.toString().includes(searchTerm) ||
      coordinate.y.toString().includes(searchTerm)
    );
  }) || [];

  return (
    <>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <Button
            variant="outline"
            className="justify-start w-full"
            disabled={disabled}
          >
            {selectedCoordinateId ? (
              <span>Coordinates ID: {selectedCoordinateId}</span>
            ) : (
              <span className="text-muted-foreground">Select Coordinates</span>
            )}
          </Button>
        </DialogTrigger>
        <DialogContent className="flex flex-col max-w-2xl max-h-[90vh] overflow-hidden">
          <DialogHeader>
            <DialogTitle>Select Coordinates</DialogTitle>
          </DialogHeader>

          <div className="flex items-center gap-2 py-4">
            <div className="relative flex-1">
              <Search className="top-2.5 left-2.5 absolute w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search coordinates (ID, X, Y)..."
                className="pl-8"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
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
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>X Coordinate</TableHead>
                      <TableHead>Y Coordinate</TableHead>
                      <TableHead className="w-[100px]">Action</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredCoordinates.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={4} className="py-8 text-muted-foreground text-center">
                          {search ? "No matching coordinates found" : "No coordinates found"}
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredCoordinates.map((coordinate) => (
                        <TableRow
                          key={coordinate.id}
                          className={`cursor-pointer hover:bg-muted ${selectedCoordinateId === coordinate.id ? "bg-accent" : ""
                            }`}
                          onClick={() => handleSelect(coordinate)}
                        >
                          <TableCell className="font-medium">{coordinate.id}</TableCell>
                          <TableCell>{coordinate.x}</TableCell>
                          <TableCell>{coordinate.y.toFixed(2)}</TableCell>
                          <TableCell>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleSelect(coordinate);
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

      <CreateCoordinateDialogContent
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onSuccess={handleCreateSuccess}
      />
    </>
  );
}
