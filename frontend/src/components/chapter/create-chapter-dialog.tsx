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
import { components } from "@/types/api.types";
import { useCreateChapter } from "@/hooks/use-chapter-hooks";
import { Loader2 } from "lucide-react";

type ChapterWithId = components["schemas"]["ChapterWithId"];


const chapterSchema = z.object({
  name: z.string().min(2, "Name must be at least 2 characters"),
  marinesCount: z
    .number()
    .min(1, "Must have at least 1 marine")
    .max(1000, "Cannot exceed 1000 marines"),
});

type ChapterFormValues = z.infer<typeof chapterSchema>;

interface CreateChapterDialogContentProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: (chapter: ChapterWithId) => void;
}

export function CreateChapterDialogContent({
  open,
  onOpenChange,
  onSuccess,
}: CreateChapterDialogContentProps) {
  const [isCreating, setIsCreating] = useState(false);
  const mutation = useCreateChapter();

  const form = useForm<ChapterFormValues>({
    resolver: zodResolver(chapterSchema),
    defaultValues: {
      name: "",
      marinesCount: 100,
    },
  });

  const onSubmit = async (data: ChapterFormValues) => {
    setIsCreating(true);
    try {
      const newChapter = await mutation.mutateAsync(data);
      toast.success(`${newChapter.name} has been established`)

      onSuccess?.(newChapter);
      form.reset();
      onOpenChange(false);
    } catch (error) {
      toast.error("Failed to create chapter", {
        description: "Please check your inputs and try again",
      });
      console.error("Chapter creation failed:", error);
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Create New Chapter</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          {/* Use onSubmit here to prevent bubbling */}
          <form
            onSubmit={(e) => {
              e.stopPropagation(); // Prevent bubbling to parent forms
              void form.handleSubmit(onSubmit)(e); // Call handleSubmit with proper event
            }}
            className="space-y-6"
          >
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Chapter Name</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="e.g. Ultramarines"
                      {...field}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.stopPropagation(); // Prevent Enter key from bubbling
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
              name="marinesCount"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Marines Count</FormLabel>
                  <FormControl>
                    <Input
                      type="number"
                      min="1"
                      max="1000"
                      {...field}
                      value={field.value || ""}
                      onChange={(e) =>
                        field.onChange(e.target.valueAsNumber || 0)
                      }
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.stopPropagation(); // Prevent Enter key from bubbling
                        }
                      }}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="button" // Important: This must be type="button"
                variant="outline"
                onClick={(e) => {
                  e.stopPropagation(); // Prevent click from bubbling
                  form.reset();
                  onOpenChange(false);
                }}
                disabled={isCreating}
              >
                Cancel
              </Button>
              <Button
                type="submit" // This submits our local form handler
                disabled={isCreating}
                onClick={(e) => {
                  e.stopPropagation(); // Additional protection
                }}
              >
                {isCreating ? (
                  <span className="flex items-center">
                    <span className="mr-2">Creating...</span>
                    <Loader2 className="w-4 h-4 animate-spin" />
                  </span>
                ) : "Create Chapter"}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
