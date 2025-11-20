"use client"

import { useState } from "react";
import { SpaceMarinesTable } from "./space-marine/space-marines-table";
import { CreateSpaceMarineDialogContent } from "./space-marine/create-space-marine-dialog";
import { Button } from "./ui/button";
import { Plus } from "lucide-react";

export function MainContent() {
    const [open, setOpen] = useState(false);

    return <div className="flex flex-col justify-center gap-2">
        <Button onClick={(e) => {
            e.stopPropagation();
            setOpen(true);
        }}>
            <Plus className="mr-2 w-4 h-4" /> Create New
        </Button>

        <SpaceMarinesTable>
        </SpaceMarinesTable>

        <CreateSpaceMarineDialogContent
            open={open}
            onOpenChange={setOpen}
        />
    </div>

}
