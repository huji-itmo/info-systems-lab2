// src/components/create-coordinate-dialog.tsx
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
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { useCreateCoordinates } from "@/hooks/use-coordinates-hooks";
import { components } from "@/types/api.types";

const coordinateSchema = z.object({
  x: z.number().min(-1000, "X coordinate must be at least -1000"),
  y: z.number()
    .min(-343, "Y coordinate must be at least -343")
    .max(343, "Y coordinate cannot exceed 343"),
});

type CoordinateFormValues = z.infer<typeof coordinateSchema>;
type CoordinatesWithId = components["schemas"]["CoordinatesWithId"];

interface CreateCoordinateDialogContentProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: (coordinate: CoordinatesWithId) => void;
}

export function CreateCoordinateDialogContent({
  open,
  onOpenChange,
  onSuccess,
}: CreateCoordinateDialogContentProps) {
  const [isCreating, setIsCreating] = useState(false);
  const mutation = useCreateCoordinates();

  const form = useForm<CoordinateFormValues>({
    resolver: zodResolver(coordinateSchema),
    defaultValues: {
      x: 0,
      y: 0,
    },
  });

  // Fixed: Properly type the parameter and use the correct variable name
  const onSubmit = async (data: CoordinateFormValues) => {
    setIsCreating(true);
    try {
      const newCoordinate = await mutation.mutateAsync({
        x: data.x,
        y: data.y
      });

      toast.success(`New coordinates at (${newCoordinate.x}, ${newCoordinate.y.toFixed(2)}`);

      onSuccess?.(newCoordinate);
      form.reset();
      onOpenChange(false);
    } catch (error) {
      toast.error("Failed to create coordinates", {
        description: "Please check your inputs and try again",
      });
      console.error("Coordinate creation failed:", error);
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Create New Coordinates</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={(e) => {
              e.stopPropagation();
              void form.handleSubmit(onSubmit)(e);
            }}
            className="space-y-6"
          >
            <div className="gap-4 grid grid-cols-2">
              <FormField
                control={form.control}
                name="x"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>X Coordinate</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        step="1"
                        {...field}
                        value={field.value || ""}
                        onChange={(e) =>
                          field.onChange(e.target.valueAsNumber || 0)
                        }
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') {
                            e.stopPropagation();
                          }
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="y"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Y Coordinate</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        step="0.01"
                        {...field}
                        value={field.value || ""}
                        onChange={(e) =>
                          field.onChange(e.target.valueAsNumber || 0)
                        }
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') {
                            e.stopPropagation();
                          }
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={(e) => {
                  e.stopPropagation();
                  form.reset();
                  onOpenChange(false);
                }}
                disabled={isCreating}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={isCreating}
                onClick={(e) => {
                  e.stopPropagation();
                }}
              >
                {isCreating ? (
                  <span className="flex items-center">
                    <span className="mr-2">Creating...</span>
                    <Loader2 className="w-4 h-4 animate-spin" />
                  </span>
                ) : "Create Coordinates"}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
