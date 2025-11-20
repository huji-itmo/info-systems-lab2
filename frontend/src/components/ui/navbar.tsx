import Link from "next/link";
import { NavigationMenu, NavigationMenuItem, NavigationMenuLink, NavigationMenuList } from "./navigation-menu";
import { cn } from "@/lib/utils";
import { ModeToggle } from "../mode-toggle";


export function Navbar({ className }: { className?: string }) {
  return (
    <div className={cn("flex justify-between", className)}>
      <NavigationMenu viewport={false}>
        <NavigationMenuList className="flex-wrap">
          <NavigationMenuItem>
            <NavigationMenuLink asChild>
              <Link href="/">Home</Link>
            </NavigationMenuLink>
          </NavigationMenuItem>
          <NavigationMenuItem>
            <NavigationMenuLink asChild>
              <Link href="/special-operations">Special operations</Link>
            </NavigationMenuLink>
          </NavigationMenuItem>
        </NavigationMenuList>
      </NavigationMenu>
      <ModeToggle></ModeToggle>
    </div>

  )
}
