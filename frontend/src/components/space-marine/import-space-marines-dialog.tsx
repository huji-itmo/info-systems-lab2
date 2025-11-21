// components/import-space-marines-dialog.tsx
"use client";
import * as React from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Upload, Loader2, CheckCircle, AlertCircle } from "lucide-react";
import { useImportSpaceMarines, ImportSummary, ImportFailure } from "@/hooks/use-import-space-marines";
import { toast } from "sonner";
import { Label } from "@/components/ui/label";
import { Progress } from "@/components/ui/progress";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";

interface ImportSpaceMarinesDialogProps {
  children?: React.ReactNode;
}

export function ImportSpaceMarinesDialog({ children }: ImportSpaceMarinesDialogProps) {
  const [isOpen, setIsOpen] = React.useState(false);
  const [file, setFile] = React.useState<File | null>(null);
  const [dragActive, setDragActive] = React.useState(false);
  const [importResult, setImportResult] = React.useState<ImportSummary | null>(null);
  const { mutate: importSpaceMarines, isPending, error } = useImportSpaceMarines();

  const handleFileDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    const droppedFile = e.dataTransfer.files[0];
    if (droppedFile) {
      validateAndSetFile(droppedFile);
    }
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      validateAndSetFile(selectedFile);
    }
    // Reset input value to allow selecting the same file again
    e.target.value = "";
  };

  const validateAndSetFile = (file: File) => {
    // Validate file type
    const validExtensions = ['json', 'xml'];
    const fileExtension = file.name.split('.').pop()?.toLowerCase();

    if (!fileExtension || !validExtensions.includes(fileExtension)) {
      toast.error("Invalid file type. Please upload a JSON or XML file.");
      return;
    }

    // Validate file size (limit to 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error("File size exceeds 5MB limit.");
      return;
    }

    setFile(file);
    setImportResult(null);
  };

  const handleImport = () => {
    if (!file) {
      toast.error("Please select a file to import.");
      return;
    }

    importSpaceMarines(file, {
      onSuccess: (result) => {
        setImportResult(result);
      }
    });
  };

  const handleReset = () => {
    setFile(null);
    setImportResult(null);
    setIsOpen(true); // Keep dialog open
  };

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      // Reset state when dialog closes
      setFile(null);
      setImportResult(null);
      setDragActive(false);
    }
    setIsOpen(open);
  };

  const getFailureMessage = (failure: ImportFailure) => {
    return `${failure.name}: ${failure.reason}`;
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        {children || (
          <Button variant="outline" className="gap-2">
            <Upload className="w-4 h-4" />
            Import Data
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[550px]">
        <DialogHeader>
          <DialogTitle>Import Space Marines</DialogTitle>
          <DialogDescription>
            Upload a JSON or XML file containing Space Marine records to import them into the system.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* File Upload Area - Only show when no result is present */}
          {!importResult && (
            <div
              className={`border-2 border-dashed rounded-lg p-6 text-center transition-all ${dragActive
                ? "border-primary bg-primary/5"
                : file
                  ? "border-green-500 bg-green-50/20"
                  : "border-border hover:border-primary/50"
                }`}
              onDragOver={(e) => {
                e.preventDefault();
                e.stopPropagation();
                setDragActive(true);
              }}
              onDragLeave={(e) => {
                e.preventDefault();
                e.stopPropagation();
                setDragActive(false);
              }}
              onDrop={handleFileDrop}
            >
              <input
                type="file"
                id="import-file"
                className="hidden"
                accept=".json,.xml"
                onChange={handleFileInputChange}
              />
              <Label
                htmlFor="import-file"
                className="flex flex-col justify-center items-center gap-3 cursor-pointer"
              >
                {file ? (
                  <>
                    <div className="bg-green-100 p-2 rounded-full">
                      <CheckCircle className="w-8 h-8 text-green-600" />
                    </div>
                    <div>
                      <p className="font-medium text-green-700">{file.name}</p>
                      <p className="text-muted-foreground text-sm">
                        {(file.size / 1024).toFixed(1)} KB
                      </p>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="bg-primary/10 p-3 rounded-full">
                      <Upload className="w-8 h-8 text-primary" />
                    </div>
                    <div>
                      <p className="font-medium">Drag & drop your file here</p>
                      <p className="mt-1 text-muted-foreground text-sm">
                        or click to browse (JSON or XML, max 5MB)
                      </p>
                    </div>
                  </>
                )}
              </Label>
            </div>
          )}

          {/* Import Result - Only show when import is complete */}
          {importResult && (
            <Card className={importResult.failed.length > 0 ? "border-destructive" : "border-green-500"}>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  {importResult.failed.length > 0 ? (
                    <>
                      <AlertCircle className="w-5 h-5 text-destructive" />
                      Import Completed with Errors
                    </>
                  ) : (
                    <>
                      <CheckCircle className="w-5 h-5 text-green-500" />
                      Import Successful
                    </>
                  )}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <p className="font-medium text-sm">Summary:</p>
                    <ul className="space-y-1 mt-2 text-sm">
                      <li className="flex justify-between">
                        <span>Total processed:</span>
                        <span className="font-medium">{importResult.total}</span>
                      </li>
                      <li className="flex justify-between text-green-600">
                        <span>Successful imports:</span>
                        <span className="font-medium">{importResult.successful}</span>
                      </li>
                      <li className="flex justify-between text-destructive">
                        <span>Failed imports:</span>
                        <span className="font-medium">{importResult.failed.length}</span>
                      </li>
                    </ul>
                  </div>

                  {importResult.failed.length > 0 && (
                    <div>
                      <p className="mb-2 font-medium text-sm">Errors:</p>
                      <ScrollArea className="p-3 border rounded-md h-[150px]">
                        <ul className="space-y-2">
                          {importResult.failed.map((failure, index) => (
                            <li key={index} className="text-destructive text-sm">
                              {getFailureMessage(failure)}
                            </li>
                          ))}
                        </ul>
                      </ScrollArea>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Progress Bar - Only show when import is in progress */}
          {isPending && (
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span>Importing file...</span>
                <span>Processing</span>
              </div>
              <Progress value={33} className="h-2" />
            </div>
          )}

          {/* Error Display */}
          {error && !isPending && !importResult && (
            <div className="bg-destructive/10 p-3 border border-destructive rounded-md text-destructive text-sm">
              {error.response?.data?.error || "An error occurred during import"}
            </div>
          )}
        </div>

        <DialogFooter className="flex gap-2">
          {importResult ? (
            <>
              <Button variant="outline" onClick={handleReset}>
                Import Another
              </Button>
              <Button onClick={() => setIsOpen(false)}>
                Done
              </Button>
            </>
          ) : (
            <>
              <Button variant="outline" onClick={() => setIsOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleImport}
                disabled={!file || isPending}
                className="gap-2"
              >
                {isPending ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Importing...
                  </>
                ) : (
                  <>
                    <Upload className="w-4 h-4" />
                    Import File
                  </>
                )}
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
