// components/edit-space-marine-dialog.tsx
"use client";

import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { SpaceMarine, useUpdateSpaceMarine } from "@/hooks/use-space-marine-hooks";
import { CoordinateSelector } from "../coordinate/coordinate-selector";
import { ChapterSelector } from "../chapter/chapter-selector";

// Update schema to match SpaceMarine structure
const spaceMarineSchema = z.object({
  name: z.string().min(1, "Name is required"),
  coordinatesId: z.number().min(1, "Coordinates ID is required"),
  chapterId: z.number().min(1, "Chapter ID is required"),
  health: z.number().min(1, "Health must be at least 1"),
  loyal: z.boolean().nullable(),
  category: z.enum(["AGGRESSOR", "INCEPTOR", "TACTICAL", "CHAPLAIN", "APOTHECARY"]).nullable(),
  weaponType: z.enum(["BOLTGUN", "HEAVY_BOLTGUN", "FLAMER", "HEAVY_FLAMER", "MULTI_MELTA"]),
});

type FormValues = z.infer<typeof spaceMarineSchema>;

interface EditSpaceMarineDialogProps {
  marine: SpaceMarine;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function EditSpaceMarineDialog({ marine, open, onOpenChange }: EditSpaceMarineDialogProps) {
  const [isUpdating, setIsUpdating] = useState(false);
  const mutation = useUpdateSpaceMarine();

  // Convert null values to undefined for form default values
  const form = useForm<FormValues>({
    resolver: zodResolver(spaceMarineSchema),
    defaultValues: {
      name: marine.name,
      coordinatesId: marine.coordinatesId,
      chapterId: marine.chapterId,
      health: marine.health,
      loyal: marine.loyal ?? false,
      category: marine.category ?? null,
      weaponType: marine.weaponType,
    },
  });

  // Fixed: Properly type the parameter and use correct variable name
  const onSubmit = async (data: FormValues) => {
    setIsUpdating(true);
    try {
      await mutation.mutateAsync({
        id: marine.id,
        ...data,
      });

      toast.success(`${data.name} has been updated`);

      onOpenChange(false);
      form.reset();
    } catch (error) {
      toast.error("Update failed", {
        description: "Please check your inputs and try again",
      });
      console.error("Update failed:", error);
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Edit Space Marine: {marine.name}</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={(e) => {
              e.stopPropagation();
              void form.handleSubmit(onSubmit)(e);
            }}
            className="space-y-6"
          >
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input placeholder="e.g. Gabriel Angelos" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="gap-4 grid grid-cols-2">
              <FormField
                control={form.control}
                name="coordinatesId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Coordinates</FormLabel>
                    <FormControl>
                      <CoordinateSelector
                        onSelect={(coordinate) => field.onChange(coordinate.id)}
                        selectedCoordinateId={field.value}
                        disabled={isUpdating}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="chapterId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Chapter</FormLabel>
                    <FormControl>
                      <ChapterSelector
                        onSelect={(chapter) => field.onChange(chapter.id)}
                        selectedChapterId={field.value}
                        disabled={isUpdating}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="gap-4 grid grid-cols-2">
              <FormField
                control={form.control}
                name="health"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Health</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        min="1"
                        {...field}
                        value={field.value || ""}
                        onChange={(e) =>
                          field.onChange(e.target.valueAsNumber || 0)
                        }
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="weaponType"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Weapon Type</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select weapon" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="BOLTGUN">Boltgun</SelectItem>
                        <SelectItem value="HEAVY_BOLTGUN">Heavy Boltgun</SelectItem>
                        <SelectItem value="FLAMER">Flamer</SelectItem>
                        <SelectItem value="HEAVY_FLAMER">Heavy Flamer</SelectItem>
                        <SelectItem value="MULTI_MELTA">Multi-Melta</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="loyal"
              render={({ field }) => (
                <FormItem className="flex flex-row items-start space-x-3 space-y-0 p-4 border rounded-md">
                  <FormControl>
                    {/* Fixed: Handle null value for checkbox */}
                    <Checkbox
                      checked={field.value ?? false}
                      onCheckedChange={field.onChange}
                      disabled={isUpdating}
                    />
                  </FormControl>
                  <div className="space-y-1 leading-none">
                    <FormLabel>Loyal to the Emperor?</FormLabel>
                  </div>
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="category"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Category</FormLabel>
                  <Select
                    value={field.value || "null"}
                    onValueChange={(value) =>
                      field.onChange(value === "null" ? null : value)
                    }
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select category" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value="null">None</SelectItem>
                      <SelectItem value="AGGRESSOR">Aggressor</SelectItem>
                      <SelectItem value="INCEPTOR">Inceptor</SelectItem>
                      <SelectItem value="TACTICAL">Tactical</SelectItem>
                      <SelectItem value="CHAPLAIN">Chaplain</SelectItem>
                      <SelectItem value="APOTHECARY">Apothecary</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={(e) => {
                  e.stopPropagation();
                  onOpenChange(false);
                  form.reset();
                }}
                disabled={isUpdating}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={isUpdating}
                className="bg-blue-600 hover:bg-blue-700"
              >
                {isUpdating ? (
                  <span className="flex items-center">
                    <span className="mr-2">Updating...</span>
                    <Loader2 className="w-4 h-4 animate-spin" />
                  </span>
                ) : "Update Marine"}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
