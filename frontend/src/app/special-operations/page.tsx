// app/special-operations/page.tsx
"use client";

import { useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
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
import { Loader2, RefreshCw, CheckCircle, AlertCircle, ArrowLeft } from "lucide-react";
import { toast } from "sonner";
import {
  useHealthSum,
  useHealthAverage,
  useFilteredSpaceMarines,
  useAssignMarineToChapter,
  useSpaceMarines,
  WeaponType,
} from "@/hooks/use-space-marine-hooks";
import { useChapters } from "@/hooks/use-chapter-hooks";
import { Badge } from "@/components/ui/badge";
import { AxiosError } from "axios";
import { useRouter } from "next/navigation";
import { ChapterSelector } from "@/components/chapter/chapter-selector";
import { Label } from "@/components/ui/label";
import { SpaceMarinesSelector } from "@/components/space-marine/space-marine-selector";

export default function SpecialOperationsPage() {
  // Health stats state
  const { data: healthSum, isLoading: isSumLoading, refetch: refetchSum } = useHealthSum();
  const { data: healthAverage, isLoading: isAvgLoading, refetch: refetchAvg } = useHealthAverage();

  // Filter by weapons state
  const [selectedWeapons, setSelectedWeapons] = useState<WeaponType[]>([]);
  const [weaponPage, setWeaponPage] = useState(0);

  const weaponSize = 10;

  const {
    data: filteredMarines,
    isLoading: isFilterLoading,
    error: filterError
  } = useFilteredSpaceMarines(selectedWeapons, weaponPage, weaponSize);

  // Assign marine to chapter state
  const [marineId, setMarineId] = useState("");
  const [chapterId, setChapterId] = useState("");
  const { mutate: assignMarine, isPending: isAssigning } = useAssignMarineToChapter();


  const handleAssign = () => {
    const marineNum = parseInt(marineId);
    const chapterNum = parseInt(chapterId);

    if (isNaN(marineNum) || isNaN(chapterNum)) {
      toast.error("Please enter valid numeric IDs");
      return;
    }

    assignMarine(
      { chapterId: chapterNum, marineId: marineNum },
      {
        onSuccess: () => {
          toast.success(`Space Marine #${marineNum} assigned to Chapter #${chapterNum}`);
          setMarineId("");
          setChapterId("");
        },
        onError: (error: AxiosError<{ error: string }>) => {
          // Handle different error structures safely
          const errorMessage = error.response?.data?.error || "Failed to assign marine to chapter";

          toast.error(errorMessage);
        },
      }
    );
  };

  const handleWeaponFilter = (weapon: WeaponType) => {
    setSelectedWeapons(prev =>
      prev.includes(weapon)
        ? prev.filter(w => w !== weapon)
        : [...prev, weapon]
    );
    setWeaponPage(0); // Reset to first page when filter changes
  };
  const router = useRouter();

  return (
    <div className="mx-auto px-4 py-8 container">
      <div className="flex gap-3">
        <Button onClick={() => router.push("/")}>
          <ArrowLeft></ArrowLeft>
          Back
        </Button>
        <h1 className="mb-8 font-bold text-3xl">Special Operations</h1>
      </div>

      {/* Health Statistics Section */}
      <div className="gap-6 grid grid-cols-1 md:grid-cols-2 mb-12">
        <Card>
          <CardHeader className="flex flex-row justify-between items-center">
            <div>
              <CardTitle>Health Sum</CardTitle>
              <CardDescription>Total health of all Space Marines</CardDescription>
            </div>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => refetchSum()}
              disabled={isSumLoading}
            >
              <RefreshCw className={`h-4 w-4 ${isSumLoading ? 'animate-spin' : ''}`} />
            </Button>
          </CardHeader>
          <CardContent>
            <div className="flex items-baseline gap-2">
              {isSumLoading ? (
                <Loader2 className="w-8 h-8 text-muted-foreground animate-spin" />
              ) : (
                <>
                  <span className="font-bold text-4xl">{healthSum ?? 0}</span>
                  <span className="text-muted-foreground">total health points</span>
                </>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row justify-between items-center">
            <div>
              <CardTitle>Health Average</CardTitle>
              <CardDescription>Average health per Space Marine</CardDescription>
            </div>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => refetchAvg()}
              disabled={isAvgLoading}
            >
              <RefreshCw className={`h-4 w-4 ${isAvgLoading ? 'animate-spin' : ''}`} />
            </Button>
          </CardHeader>
          <CardContent>
            <div className="flex items-baseline gap-2">
              {isAvgLoading ? (
                <Loader2 className="w-8 h-8 text-muted-foreground animate-spin" />
              ) : (
                <>
                  <span className="font-bold text-4xl">{healthAverage?.toFixed(2) ?? 0}</span>
                  <span className="text-muted-foreground">average health</span>
                </>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filter by Weapons Section */}
      <Card className="mb-12">
        <CardHeader>
          <CardTitle>Filter Space Marines by Weapons</CardTitle>
          <CardDescription>Select weapon types to filter Space Marines</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-6">
            <div className="flex flex-wrap gap-2 mb-4">
              {(["BOLTGUN", "HEAVY_BOLTGUN", "FLAMER", "HEAVY_FLAMER", "MULTI_MELTA"] as WeaponType[]).map((weapon) => (
                <Badge
                  key={weapon}
                  variant={selectedWeapons.includes(weapon) ? "default" : "outline"}
                  className="hover:bg-primary/80 transition-colors cursor-pointer"
                  onClick={() => handleWeaponFilter(weapon)}
                >
                  {weapon.replace(/_/g, ' ')}
                </Badge>
              ))}
            </div>

            {selectedWeapons.length === 0 && (
              <p className="mb-4 text-muted-foreground text-sm">
                Select one or more weapon types to see filtered results
              </p>
            )}
          </div>

          {selectedWeapons.length > 0 && (
            <>
              {isFilterLoading ? (
                <div className="flex justify-center py-12">
                  <Loader2 className="w-8 h-8 text-muted-foreground animate-spin" />
                </div>
              ) : filterError ? (
                <div className="flex flex-col justify-center items-center py-12 text-destructive">
                  <AlertCircle className="mb-2 w-8 h-8" />
                  <p>Failed to load filtered marines</p>
                  <Button
                    variant="outline"
                    size="sm"
                    className="mt-2"
                    onClick={() => window.location.reload()}
                  >
                    Retry
                  </Button>
                </div>
              ) : filteredMarines?.content.length === 0 ? (
                <div className="flex flex-col justify-center items-center py-12 text-muted-foreground">
                  <p>No Space Marines found with the selected weapons</p>
                </div>
              ) : (
                <>
                  <div className="mb-4 border rounded-md overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>ID</TableHead>
                          <TableHead>Name</TableHead>
                          <TableHead>Chapter</TableHead>
                          <TableHead>Weapon</TableHead>
                          <TableHead>Health</TableHead>
                          <TableHead>Category</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {filteredMarines?.content.map((marine) => (
                          <TableRow key={marine.id}>
                            <TableCell className="font-mono">{marine.id}</TableCell>
                            <TableCell>{marine.name}</TableCell>
                            <TableCell className="font-mono">{marine.chapterId}</TableCell>
                            <TableCell>
                              <Badge variant="secondary">
                                {marine.weaponType.replace(/_/g, ' ')}
                              </Badge>
                            </TableCell>
                            <TableCell>{marine.health}</TableCell>
                            <TableCell>
                              {marine.category ? (
                                <Badge variant="outline">
                                  {marine.category}
                                </Badge>
                              ) : (
                                <span className="text-muted-foreground">—</span>
                              )}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>

                  {filteredMarines && filteredMarines.totalPages > 1 && (
                    <div className="flex justify-center mt-4">
                      <Pagination>
                        <PaginationContent>
                          <PaginationItem>
                            <PaginationPrevious
                              onClick={() => setWeaponPage(prev => Math.max(0, prev - 1))}
                              className={weaponPage === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                          </PaginationItem>
                          {[...Array(filteredMarines.totalPages)].map((_, index) => (
                            <PaginationItem key={index}>
                              <PaginationLink
                                onClick={() => setWeaponPage(index)}
                                isActive={weaponPage === index}
                              >
                                {index + 1}
                              </PaginationLink>
                            </PaginationItem>
                          ))}
                          <PaginationItem>
                            <PaginationNext
                              onClick={() => setWeaponPage(prev => Math.min(filteredMarines.totalPages - 1, prev + 1))}
                              className={weaponPage === filteredMarines.totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                          </PaginationItem>
                        </PaginationContent>
                      </Pagination>
                    </div>
                  )}
                </>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {/* Assign Marine to Chapter Section */}
      <Card>
        <CardHeader>
          <CardTitle>Assign Space Marine to Chapter</CardTitle>
          <CardDescription>Move a Space Marine to a different Chapter</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="gap-6 grid grid-cols-1 md:grid-cols-2">
            <div className="space-y-2">
              <Label className="font-medium text-sm">Space Marine ID</Label>
              <SpaceMarinesSelector
                onSelect={spaceMarine => setMarineId(spaceMarine.id.toString())}
                selectedSpaceMarineId={Number(marineId)}
                disabled={isAssigning}
              />

            </div>

            <div className="space-y-2">
              <Label className="font-medium text-sm">Chapter</Label>
              <ChapterSelector
                onSelect={chapter => setChapterId(chapter.id.toString())}
                selectedChapterId={Number(chapterId)}
                disabled={isAssigning}
              />
            </div>
          </div>

          <div className="flex justify-end mt-6">
            <Button
              onClick={handleAssign}
              disabled={isAssigning || !marineId || !chapterId}
              className="bg-purple-600 hover:bg-purple-700"
            >
              {isAssigning ? (
                <span className="flex items-center">
                  <Loader2 className="mr-2 w-4 h-4 animate-spin" />
                  Assigning...
                </span>
              ) : (
                <span className="flex items-center">
                  <CheckCircle className="mr-2 w-4 h-4" />
                  Assign Marine to Chapter
                </span>
              )}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
