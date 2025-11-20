"use client";

import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
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
import { spaceMarineSchema } from "@/lib/schemas";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { ChapterSelector } from "@/components/chapter/chapter-selector"; // Import the ChapterSelector
import { CoordinateSelector } from "../coordinate/coordinate-selector";
import { SpaceMarine, useCreateSpaceMarine } from "@/hooks/use-space-marine-hooks";

// Define the correct type for form submission
type FormValues = z.infer<typeof spaceMarineSchema>;

interface CreateSpaceMarineDialogContentProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: (coordinate: SpaceMarine) => void;
}


export function CreateSpaceMarineDialogContent({
  open,
  onOpenChange,
  onSuccess,
}: CreateSpaceMarineDialogContentProps) {
  const createSpaceMarine = useCreateSpaceMarine();

  const form = useForm<FormValues>({
    resolver: zodResolver(spaceMarineSchema),
    defaultValues: {
      name: "",
      coordinatesId: 1,
      chapterId: 1,
      health: 100,
      loyal: true,
      category: null,
      weaponType: "BOLTGUN",
    },
  });

  const onSubmit = async (data: FormValues) => {
    try {
      const new_marine = await createSpaceMarine.mutateAsync(data);
      toast.success("Space Marine created successfully");

      onSuccess?.(new_marine);
      form.reset();
      onOpenChange(false);
    } catch (error) {
      toast.error("Failed to create coordinates", {
        description: "Please check your inputs and try again",
      });
      console.error("Coordinate creation failed:", error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Add New Space Marine</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
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
                    <FormLabel>Coordinates ID</FormLabel>
                    <FormControl>
                      <CoordinateSelector
                        onSelect={(coordinates) => field.onChange(coordinates.id)}
                        selectedCoordinateId={field.value}
                        disabled={createSpaceMarine.isPending}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Replace Chapter ID input with ChapterSelector */}
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
                        disabled={createSpaceMarine.isPending}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

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
              name="loyal"
              render={({ field }) => (
                <FormItem className="flex flex-row items-start space-x-3 space-y-0 p-4 border rounded-md">
                  <FormControl>
                    <Checkbox
                      checked={field.value}
                      onCheckedChange={field.onChange}
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

            <div className="flex justify-end gap-2 pt-4">
              <DialogClose asChild>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    onOpenChange(false);
                    form.reset();
                  }}
                >
                  Cancel
                </Button>
              </DialogClose>

              <Button
                type="submit"
                disabled={createSpaceMarine.isPending}
                className="bg-emerald-600 hover:bg-emerald-700"
              >
                {createSpaceMarine.isPending ? (
                  <span className="flex items-center">
                    <span className="mr-2">Creating...</span>
                    <span className="inline-block border-2 border-white border-t-transparent rounded-full w-4 h-4 animate-spin" />
                  </span>
                ) : "Create Marine"}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
